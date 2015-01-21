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
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHAgentAuthenticator;
import ch.cyberduck.core.sftp.putty.PageantAuthenticator;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.schmizz.concurrent.Promise;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.compression.Compression;
import net.schmizz.sshj.transport.compression.DelayedZlibCompression;
import net.schmizz.sshj.transport.compression.NoneCompression;
import net.schmizz.sshj.transport.compression.ZlibCompression;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * @version $Id$
 */
public class SFTPSession extends Session<SSHClient> {
    private static final Logger log = Logger.getLogger(SFTPSession.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private SFTPEngine sftp;

    private StateDisconnectListener disconnectListener;

    public SFTPSession(final Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        if(super.isConnected()) {
            return client.isConnected();
        }
        return false;
    }

    /**
     * @return True if authentication is complete
     */
    @Override
    public boolean isSecured() {
        if(super.isSecured()) {
            return client.isAuthenticated();
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
                configuration.setCompressionFactories(Arrays.<Factory.Named<Compression>>asList(
                        new NoneCompression.Factory()));
            }
            configuration.setVersion(new PreferencesUseragentProvider().get());
            final KeepAliveProvider heartbeat = KeepAliveProvider.HEARTBEAT;
            configuration.setKeepAliveProvider(heartbeat);
            return this.connect(key, configuration);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    protected SSHClient connect(final HostKeyCallback key, final DefaultConfig configuration) throws IOException {
        final SSHClient connection = new SSHClient(configuration);
        final int timeout = this.timeout();
        connection.setTimeout(timeout);
        connection.setConnectTimeout(timeout);
        connection.setSocketFactory(new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return host.getHostname();
            }
        }));
        connection.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String hostname, int port, PublicKey publicKey) {
                try {
                    return key.verify(hostname, port, publicKey);
                }
                catch(ConnectionCanceledException e) {
                    return false;
                }
                catch(ChecksumException e) {
                    return false;
                }
            }
        });
        disconnectListener = new StateDisconnectListener();
        final Transport transport = connection.getTransport();
        transport.setDisconnectListener(disconnectListener);
        connection.connect(HostnameConfiguratorFactory.get(host.getProtocol()).getHostname(host.getHostname()), host.getPort());
        connection.getConnection().getKeepAlive().setKeepAliveInterval(
                preferences.getInteger("ssh.heartbeat.seconds")
        );
        return connection;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        final List<SFTPAuthentication> methods = new ArrayList<SFTPAuthentication>();
        final Credentials credentials = host.getCredentials();
        if(credentials.isAnonymousLogin()) {
            methods.add(new SFTPNoneAuthentication(this));
        }
        else {
            if(credentials.isPublicKeyAuthentication()) {
                methods.add(new SFTPPublicKeyAuthentication(this));
            }
            else {
                methods.add(new SFTPChallengeResponseAuthentication(this));
                methods.add(new SFTPPasswordAuthentication(this));
                if(preferences.getBoolean("ssh.authentication.agent.enable")) {
                    methods.add(new SFTPAgentAuthentication(this, new OpenSSHAgentAuthenticator()));
                    methods.add(new SFTPAgentAuthentication(this, new PageantAuthenticator()));
                }
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
            catch(IllegalStateException s) {
                log.warn(String.format("Server disconnected with %s while trying authentication method %s",
                        disconnectListener.getFailure(), auth));
                try {
                    throw new SFTPExceptionMappingService().map(LocaleFactory.localizedString("Login failed", "Credentials"),
                            disconnectListener.getFailure());
                }
                catch(InteroperabilityException e) {
                    throw new LoginFailureException(e.getMessage(), e);
                }
            }
            catch(InteroperabilityException e) {
                throw new LoginFailureException(e.getMessage(), e);
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
            this.log(false, banner);
        }
        // Check if authentication is partial
        if(!client.isAuthenticated()) {
            if(client.getUserAuth().hadPartialSuccess()) {
                final Credentials additional = new HostCredentials(host, null, null, false);
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
            sftp = new SFTPEngine(client, String.valueOf(Path.DELIMITER), preferences.getProperty("ssh.subsystem.name")) {
                @Override
                public Promise<Response, SFTPException> request(final Request req) throws IOException {
                    log(true, String.format("%d %s", req.getRequestID(), req.getType()));
                    return super.request(req);
                }
            }.init();
            sftp.setTimeoutMs(this.timeout());
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
    public Path workdir() throws BackgroundException {
        // "." as referring to the current directory
        final String directory;
        try {
            directory = this.sftp().canonicalize(".");
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
        return new Path(directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ?
                        EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory)
        );
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new SFTPListService(this).list(directory, listener);
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Attributes.class) {
            return (T) new SFTPAttributesFeature(this);
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
        return super.getFeature(type);
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