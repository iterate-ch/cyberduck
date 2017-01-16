package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CustomOriginCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHAgentAuthenticator;
import ch.cyberduck.core.sftp.putty.PageantAuthenticator;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.schmizz.concurrent.Promise;
import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.NegotiatedAlgorithms;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.compression.DelayedZlibCompression;
import net.schmizz.sshj.transport.compression.NoneCompression;
import net.schmizz.sshj.transport.compression.ZlibCompression;
import net.schmizz.sshj.transport.verification.AlgorithmsVerifier;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class SFTPSession extends Session<SSHClient> {
    private static final Logger log = Logger.getLogger(SFTPSession.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private SFTPEngine sftp;

    private StateDisconnectListener disconnectListener;

    private NegotiatedAlgorithms algorithms;

    private final SocketFactory socketFactory;

    public SFTPSession(final Host h) {
        this(h, new ProxySocketFactory(h.getProtocol(), new DefaultTrustManagerHostnameCallback(h)));
    }

    public SFTPSession(final Host h, final ProxyFinder proxy) {
        this(h, new ProxySocketFactory(h.getProtocol(), new DefaultTrustManagerHostnameCallback(h), proxy));
    }

    public SFTPSession(final Host h, final SocketFactory socketFactory) {
        super(h);
        this.socketFactory = socketFactory;
    }

    @Override
    public boolean isConnected() {
        if(super.isConnected()) {
            return client.isConnected();
        }
        return false;
    }

    @Override
    public SSHClient connect(final HostKeyCallback key) throws BackgroundException {
        try {
            final DefaultConfig configuration = new DefaultConfig();
            if("zlib".equals(preferences.getProperty("ssh.compression"))) {
                configuration.setCompressionFactories(Arrays.asList(
                        new DelayedZlibCompression.Factory(),
                        new ZlibCompression.Factory(),
                        new NoneCompression.Factory()));
            }
            else {
                configuration.setCompressionFactories(Collections.singletonList(new NoneCompression.Factory()));
            }
            configuration.setVersion(new PreferencesUseragentProvider().get());
            final KeepAliveProvider heartbeat;
            if(preferences.getProperty("ssh.heartbeat.provider").equals("keep-alive")) {
                heartbeat = KeepAliveProvider.KEEP_ALIVE;
            }
            else {
                heartbeat = KeepAliveProvider.HEARTBEAT;
            }
            configuration.setKeepAliveProvider(heartbeat);
            return this.connect(key, configuration);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    protected SSHClient connect(final HostKeyCallback key, final Config configuration) throws IOException {
        final SSHClient connection = new SSHClient(configuration);
        final int timeout = preferences.getInteger("connection.timeout.seconds") * 1000;
        connection.setTimeout(timeout);
        connection.setSocketFactory(socketFactory);
        connection.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String hostname, int port, PublicKey publicKey) {
                try {
                    return key.verify(hostname, port, publicKey);
                }
                catch(ConnectionCanceledException | ChecksumException e) {
                    return false;
                }
            }
        });
        connection.addAlgorithmsVerifier(new AlgorithmsVerifier() {
            @Override
            public boolean verify(final NegotiatedAlgorithms negotiatedAlgorithms) {
                log.info(String.format("Negotiated algorithms %s", negotiatedAlgorithms));
                algorithms = negotiatedAlgorithms;
                return true;
            }
        });
        disconnectListener = new StateDisconnectListener();
        final Transport transport = connection.getTransport();
        transport.setDisconnectListener(disconnectListener);
        connection.connect(HostnameConfiguratorFactory.get(host.getProtocol()).getHostname(host.getHostname()), host.getPort());
        final KeepAlive keepalive = connection.getConnection().getKeepAlive();
        keepalive.setKeepAliveInterval(preferences.getInteger("ssh.heartbeat.seconds"));
        return connection;
    }

    @Override
    public boolean alert(final ConnectionCallback prompt) throws BackgroundException {
        if(null == algorithms) {
            return super.alert(prompt);
        }
        if(!preferences.getBoolean(String.format("ssh.algorithm.whitelist.%s", host.getHostname()))) {
            if(preferences.getList("ssh.algorithm.cipher.blacklist").contains(algorithms.getClient2ServerCipherAlgorithm())) {
                alert(prompt, algorithms.getClient2ServerCipherAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.cipher.blacklist").contains(algorithms.getServer2ClientCipherAlgorithm())) {
                alert(prompt, algorithms.getServer2ClientCipherAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.mac.blacklist").contains(algorithms.getClient2ServerMACAlgorithm())) {
                alert(prompt, algorithms.getClient2ServerMACAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.mac.blacklist").contains(algorithms.getServer2ClientMACAlgorithm())) {
                alert(prompt, algorithms.getServer2ClientMACAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.kex.blacklist").contains(algorithms.getKeyExchangeAlgorithm())) {
                alert(prompt, algorithms.getKeyExchangeAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.signature.blacklist").contains(algorithms.getSignatureAlgorithm())) {
                alert(prompt, algorithms.getSignatureAlgorithm());
            }
        }
        return super.alert(prompt);
    }

    private void alert(final ConnectionCallback prompt, final String algorithm) throws ConnectionCanceledException {
        prompt.warn(host.getProtocol(), MessageFormat.format(LocaleFactory.localizedString("Insecure algorithm {0} negotiated with server", "Credentials"),
                algorithm),
                MessageFormat.format("{0}. {1}.", LocaleFactory.localizedString("The algorithm is possibly too weak to meet current cryptography standards", "Credentials"),
                        LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                LocaleFactory.localizedString("Continue", "Credentials"),
                LocaleFactory.localizedString("Disconnect", "Credentials"),
                String.format("ssh.algorithm.whitelist.%s", host.getHostname()));
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        final List<SFTPAuthentication> methods = new ArrayList<SFTPAuthentication>();
        final Credentials credentials = host.getCredentials();
        if(credentials.isAnonymousLogin()) {
            methods.add(new SFTPNoneAuthentication(this));
        }
        else {
            if(preferences.getBoolean("ssh.authentication.agent.enable")) {
                methods.add(new SFTPAgentAuthentication(this, new OpenSSHAgentAuthenticator()));
                methods.add(new SFTPAgentAuthentication(this, new PageantAuthenticator()));
            }
            if(credentials.isPublicKeyAuthentication()) {
                methods.add(new SFTPPublicKeyAuthentication(this, keychain));
            }
            else {
                methods.add(new SFTPChallengeResponseAuthentication(this));
                methods.add(new SFTPPasswordAuthentication(this));
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt login with %d authentication methods", methods.size()));
        }
        BackgroundException lastFailure = null;
        for(SFTPAuthentication auth : methods) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication with credentials %s and authentication method %s", credentials, auth));
            }
            cancel.verify();
            try {
                if(!auth.authenticate(host, prompt, cancel)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Login refused with credentials %s and authentication method %s", credentials, auth));
                    }
                    continue;
                }
            }
            catch(IllegalStateException ignored) {
                log.warn(String.format("Server disconnected with %s while trying authentication method %s",
                        disconnectListener.getFailure(), auth));
                try {
                    throw new SFTPExceptionMappingService().map(LocaleFactory.localizedString("Login failed", "Credentials"),
                            disconnectListener.getFailure());
                }
                catch(InteroperabilityException e) {
                    throw new LoginFailureException(e.getDetail(false), e);
                }
            }
            catch(InteroperabilityException e) {
                throw new LoginFailureException(e.getDetail(false), e);
            }
            catch(LoginFailureException e) {
                log.warn(String.format("Login failed with credentials %s and authentication method %s", credentials, auth));
                if(!client.isConnected()) {
                    log.warn(String.format("Server disconnected after failed authentication attempt with method %s", auth));
                    // No more connected. When changing the username the connection is closed by the server.
                    throw e;
                }
                lastFailure = e;
                continue;
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Login successful with authentication method %s", auth));
            }
            break;
        }
        final String banner = client.getUserAuth().getBanner();
        if(StringUtils.isNotBlank(banner)) {
            this.log(Type.response, banner);
        }
        // Check if authentication is partial
        if(!client.isAuthenticated()) {
            if(client.getUserAuth().hadPartialSuccess()) {
                final Credentials additional = new HostCredentials(host, credentials.getUsername());
                prompt.prompt(host, additional,
                        LocaleFactory.localizedString("Partial authentication success", "Credentials"),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        new LoginOptions().user(false).keychain(false).publickey(false));
                if(!new SFTPChallengeResponseAuthentication(this).authenticate(host, additional, prompt)) {
                    throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                            "Login {0} with username and password", "Credentials"), host.getHostname()));
                }
            }
            else {
                if(null == lastFailure) {
                    throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                            "Login {0} with username and password", "Credentials"), host.getHostname()));
                }
                throw lastFailure;
            }
        }
        try {
            sftp = new SFTPEngine(client, String.valueOf(Path.DELIMITER)) {
                @Override
                public Promise<Response, SFTPException> request(final Request req) throws IOException {
                    log(Type.request, String.format("%d %s", req.getRequestID(), req.getType()));
                    return super.request(req);
                }
            }.init();
            final int timeout = preferences.getInteger("connection.timeout.seconds") * 1000;
            sftp.setTimeoutMs(timeout);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    public SFTPEngine sftp() throws LoginCanceledException {
        if(null == sftp) {
            throw new LoginCanceledException();
        }
        return sftp;
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            if(null == sftp) {
                return;
            }
            sftp.close();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
        finally {
            super.logout();
        }
    }

    @Override
    public void disconnect() {
        try {
            client.close();
        }
        catch(IOException e) {
            log.warn(String.format("Ignore disconnect failure %s", e.getMessage()));
        }
        super.disconnect();
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new SFTPListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Attributes.class) {
            return (T) new SFTPAttributesFinderFeature(this);
        }
        if(type == Read.class) {
            return (T) new SFTPReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new SFTPWriteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new SFTPDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SFTPDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new SFTPMoveFeature(this);
        }
        if(type == UnixPermission.class) {
            return (T) new SFTPUnixPermissionFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new SFTPTimestampFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SFTPTouchFeature(this);
        }
        if(type == Symlink.class) {
            return (T) new SFTPSymlinkFeature(this);
        }
        if(type == Command.class) {
            return (T) new SFTPCommandFeature(this);
        }
        if(type == Compress.class) {
            return (T) new SFTPCompressFeature(this);
        }
        if(type == DistributionConfiguration.class) {
            return (T) new CustomOriginCloudFrontDistributionConfiguration(host, this);
        }
        if(type == Home.class) {
            return (T) new SFTPHomeDirectoryService(this);
        }
        if(type == Quota.class) {
            return (T) new SFTPQuotaFeature(this);
        }
        return super._getFeature(type);
    }

    private static final class StateDisconnectListener implements DisconnectListener {
        private SSHException failure;

        @Override
        public void notifyDisconnect(final DisconnectReason reason, final String message) {
            log.warn(String.format("Disconnected %s", reason));
            failure = new SSHException(reason, message);
        }

        /**
         * @return Last disconnect reason
         */
        public SSHException getFailure() {
            return failure;
        }
    }
}