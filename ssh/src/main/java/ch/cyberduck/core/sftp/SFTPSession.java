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
import ch.cyberduck.core.Factory.Platform;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CustomOriginCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.sftp.auth.SFTPAgentAuthentication;
import ch.cyberduck.core.sftp.auth.SFTPChallengeResponseAuthentication;
import ch.cyberduck.core.sftp.auth.SFTPNoneAuthentication;
import ch.cyberduck.core.sftp.auth.SFTPPasswordAuthentication;
import ch.cyberduck.core.sftp.auth.SFTPPublicKeyAuthentication;
import ch.cyberduck.core.sftp.compression.JcraftDelayedZlibCompression;
import ch.cyberduck.core.sftp.compression.JcraftZlibCompression;
import ch.cyberduck.core.sftp.openssh.OpenSSHAgentAuthenticator;
import ch.cyberduck.core.sftp.openssh.OpenSSHCredentialsConfigurator;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostnameConfigurator;
import ch.cyberduck.core.sftp.openssh.OpenSSHIdentityAgentConfigurator;
import ch.cyberduck.core.sftp.openssh.OpenSSHJumpHostConfigurator;
import ch.cyberduck.core.sftp.openssh.OpenSSHPreferredAuthenticationsConfigurator;
import ch.cyberduck.core.sftp.openssh.WindowsOpenSSHAgentAuthenticator;
import ch.cyberduck.core.sftp.putty.PageantAuthenticator;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.TildeResolvingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.jcraft.jsch.agentproxy.AgentProxyException;
import net.schmizz.concurrent.Promise;
import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.Factory.Named;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.DirectConnection;
import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.NegotiatedAlgorithms;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.cipher.Cipher;
import net.schmizz.sshj.transport.compression.NoneCompression;
import net.schmizz.sshj.transport.verification.AlgorithmsVerifier;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class SFTPSession extends Session<SSHClient> {
    private static final Logger log = LogManager.getLogger(SFTPSession.class);

    private final PreferencesReader preferences = new HostPreferences(host);

    private SFTPEngine sftp;
    private StateDisconnectListener disconnectListener;
    private NegotiatedAlgorithms algorithms;

    private final X509TrustManager trust;
    private final X509KeyManager key;

    public SFTPSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
        this.trust = trust;
        this.key = key;
    }

    @Override
    public boolean isConnected() {
        if(super.isConnected()) {
            return client.isConnected();
        }
        return false;
    }

    @Override
    protected SSHClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final DefaultConfig configuration = new DefaultConfig();
        if("zlib".equals(preferences.getProperty("ssh.compression"))) {
            configuration.setCompressionFactories(Arrays.asList(
                    new JcraftDelayedZlibCompression.Factory(),
                    new JcraftZlibCompression.Factory(),
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
        configuration.setCipherFactories(this.lowestPriorityForCBC(configuration.getCipherFactories()));
        return this.connect(key, prompt, configuration);
    }

    protected SSHClient connect(final HostKeyCallback key, final LoginCallback prompt, final Config configuration) throws BackgroundException {
        final SSHClient connection = this.toClient(key, configuration);
        try {
            // Look for jump host configuration
            final Host proxy = new OpenSSHJumpHostConfigurator().getJumphost(host.getHostname());
            if(null != proxy) {
                log.info("Connect using jump host configuration {}", proxy);
                final SSHClient hop = this.toClient(key, configuration);
                hop.connect(proxy.getHostname(), proxy.getPort());
                final Credentials proxyCredentials = new OpenSSHCredentialsConfigurator().configure(proxy);
                proxy.setCredentials(proxyCredentials);
                final KeychainLoginService service = new KeychainLoginService();
                service.validate(proxy, prompt, new LoginOptions(proxy.getProtocol()));
                // Authenticate with jump host
                this.authenticate(hop, proxy, prompt, new DisabledCancelCallback());
                log.debug("Authenticated with jump host {}", proxy);
                // Write credentials to keychain
                service.save(proxy);
                final DirectConnection tunnel = hop.newDirectConnection(
                        new OpenSSHHostnameConfigurator().getHostname(host.getHostname()), host.getPort());
                // Connect to internal host
                connection.connectVia(tunnel);
            }
            else {
                connection.connect(new OpenSSHHostnameConfigurator().getHostname(host.getHostname()), host.getPort());
            }
            final KeepAlive keepalive = connection.getConnection().getKeepAlive();
            keepalive.setKeepAliveInterval(preferences.getInteger("ssh.heartbeat.seconds"));
            return connection;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    private SSHClient toClient(final HostKeyCallback key, final Config configuration) {
        final SSHClient connection = new SSHClient(configuration);
        final int timeout = ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000;
        connection.getTransport().setTimeoutMs(timeout);
        connection.setTimeout(timeout);
        connection.setSocketFactory(new ProxySocketFactory(host));
        connection.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String hostname, int port, PublicKey publicKey) {
                try {
                    return key.verify(host, publicKey);
                }
                catch(BackgroundException e) {
                    return false;
                }
            }

            @Override
            public List<String> findExistingAlgorithms(final String hostname, final int port) {
                return Collections.emptyList();
            }
        });
        connection.addAlgorithmsVerifier(new AlgorithmsVerifier() {
            @Override
            public boolean verify(final NegotiatedAlgorithms negotiatedAlgorithms) {
                log.info("Negotiated algorithms {}", negotiatedAlgorithms);
                algorithms = negotiatedAlgorithms;
                return true;
            }
        });
        final Charset charset = Charset.forName(host.getEncoding());
        log.debug("Use character encoding {}", charset);
        connection.setRemoteCharset(charset);
        disconnectListener = new StateDisconnectListener();
        final Transport transport = connection.getTransport();
        transport.setDisconnectListener(disconnectListener);
        return connection;
    }

    @Override
    public boolean alert(final ConnectionCallback prompt) throws BackgroundException {
        if(null == algorithms) {
            return super.alert(prompt);
        }
        if(!preferences.getBoolean(String.format("ssh.algorithm.whitelist.%s", host.getHostname()))) {
            if(preferences.getList("ssh.algorithm.cipher.blacklist").contains(algorithms.getClient2ServerCipherAlgorithm())) {
                this.alert(prompt, algorithms.getClient2ServerCipherAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.cipher.blacklist").contains(algorithms.getServer2ClientCipherAlgorithm())) {
                this.alert(prompt, algorithms.getServer2ClientCipherAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.mac.blacklist").contains(algorithms.getClient2ServerMACAlgorithm())) {
                this.alert(prompt, algorithms.getClient2ServerMACAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.mac.blacklist").contains(algorithms.getServer2ClientMACAlgorithm())) {
                this.alert(prompt, algorithms.getServer2ClientMACAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.kex.blacklist").contains(algorithms.getKeyExchangeAlgorithm())) {
                this.alert(prompt, algorithms.getKeyExchangeAlgorithm());
            }
            if(preferences.getList("ssh.algorithm.signature.blacklist").contains(algorithms.getSignatureAlgorithm())) {
                this.alert(prompt, algorithms.getSignatureAlgorithm());
            }
        }
        return super.alert(prompt);
    }

    private void alert(final ConnectionCallback prompt, final String algorithm) throws ConnectionCanceledException {
        prompt.warn(host, MessageFormat.format(LocaleFactory.localizedString("Insecure algorithm {0} negotiated with server", "Credentials"),
                        algorithm),
                new StringAppender()
                        .append(LocaleFactory.localizedString("The algorithm is possibly too weak to meet current cryptography standards", "Credentials"))
                        .append(LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")).toString(),
                LocaleFactory.localizedString("Continue", "Credentials"),
                LocaleFactory.localizedString("Disconnect", "Credentials"),
                String.format("ssh.algorithm.whitelist.%s", host.getHostname()));
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        this.authenticate(client, host, prompt, cancel);
        try {
            sftp = new LoggingSFTPEngine(client, this).init();
            sftp.setTimeoutMs(ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    private void authenticate(final SSHClient client, final Host host, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        try {
            if(new SFTPNoneAuthentication(client).authenticate(host, prompt, cancel)) {
                return;
            }
        }
        catch(LoginFailureException e) {
            // Expected. The main purpose of sending this request is to get the list of supported methods from the server
        }
        // Ordered list of preferred authentication methods
        final List<AuthenticationProvider<Boolean>> defaultMethods = new ArrayList<>();
        if(preferences.getBoolean("ssh.authentication.agent.enable")) {
            final String identityAgent = new OpenSSHIdentityAgentConfigurator().getIdentityAgent(host.getHostname());
            switch(Platform.getDefault()) {
                case windows:
                    defaultMethods.add(new SFTPAgentAuthentication(client, new PageantAuthenticator()));
                    try {
                        defaultMethods.add(new SFTPAgentAuthentication(client,
                                new WindowsOpenSSHAgentAuthenticator(identityAgent)));
                    }
                    catch(AgentProxyException e) {
                        log.warn("Agent proxy failed with {}", e);
                    }
                    break;
                default:
                    try {
                        defaultMethods.add(new SFTPAgentAuthentication(client,
                                new OpenSSHAgentAuthenticator(identityAgent)));
                    }
                    catch(AgentProxyException e) {
                        log.warn("Agent proxy failed with {}", e);
                    }
                    break;
            }
        }
        defaultMethods.add(new SFTPPublicKeyAuthentication(client));
        if(credentials.isPasswordAuthentication()) {
            defaultMethods.add(0, new SFTPPasswordAuthentication(client));
            defaultMethods.add(0, new SFTPChallengeResponseAuthentication(client));
        }
        else {
            defaultMethods.add(new SFTPChallengeResponseAuthentication(client));
            defaultMethods.add(new SFTPPasswordAuthentication(client));
        }
        final LinkedHashMap<String, List<AuthenticationProvider<Boolean>>> methodsMap = new LinkedHashMap<>();
        defaultMethods.forEach(m -> methodsMap.computeIfAbsent(m.getMethod(), k -> new ArrayList<>()).add(m));
        final List<AuthenticationProvider<Boolean>> methods = new ArrayList<>();
        final String[] preferred = new OpenSSHPreferredAuthenticationsConfigurator().getPreferred(host.getHostname());
        if(preferred != null) {
            log.debug("Filter authentication methods with {}", Arrays.toString(preferred));
            for(String p : preferred) {
                final List<AuthenticationProvider<Boolean>> providers = methodsMap.remove(p);
                if(providers != null) {
                    methods.addAll(providers);
                }
            }
        }
        methodsMap.values().forEach(methods::addAll);
        log.debug("Attempt login with {} authentication methods {}", methods.size(), Arrays.toString(methods.toArray()));
        BackgroundException lastFailure = null;
        for(AuthenticationProvider<Boolean> auth : methods) {
            cancel.verify();
            try {
                // Obtain latest list of allowed methods
                final Collection<String> allowed = client.getUserAuth().getAllowedMethods();
                log.debug("Remaining authentication methods {}", Arrays.toString(allowed.toArray()));
                if(!allowed.contains(auth.getMethod())) {
                    log.warn("Skip authentication method {} not allowed", auth);
                    continue;
                }
                log.info("Attempt authentication with credentials {} and authentication method {}", credentials, auth);
                try {
                    if(auth.authenticate(host, prompt, cancel)) {
                        log.info("Authentication succeeded with credentials {} and authentication method {}", credentials, auth);
                        // Success
                        break;
                    }
                    else {
                        // Check if authentication is partial
                        if(client.getUserAuth().hadPartialSuccess()) {
                            log.info("Partial login success with credentials {} and authentication method {}", credentials, auth);
                        }
                        else {
                            log.warn("Login refused with credentials {} and authentication method {}", credentials, auth);
                        }
                        // Continue trying next authentication method
                    }
                }
                catch(LoginFailureException e) {
                    log.warn("Login failed with credentials {} and authentication method {}", credentials, auth);
                    if(!client.isConnected()) {
                        log.warn("Server disconnected after failed authentication attempt with method {}", auth);
                        // No more connected. When changing the username the connection is closed by the server.
                        throw e;
                    }
                    lastFailure = e;
                }
            }
            catch(IllegalStateException ignored) {
                log.warn("Server disconnected with {} while trying authentication method {}", disconnectListener.getFailure(), auth);
                try {
                    if(null == disconnectListener.getFailure()) {
                        throw new ConnectionRefusedException(LocaleFactory.localizedString("Login failed", "Credentials"), ignored);
                    }
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
        }
        if(!client.isAuthenticated()) {
            if(null == lastFailure) {
                throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(host)));
            }
            throw lastFailure;
        }
        final String banner = client.getUserAuth().getBanner();
        if(StringUtils.isNotBlank(banner)) {
            this.log(Type.response, banner);
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
            log.warn("Ignore disconnect failure {}", e.getMessage());
        }
        super.disconnect();
    }

    private List<Named<Cipher>> lowestPriorityForCBC(final List<Named<Cipher>> factories) {
        return factories.stream().sorted((c1, c2) -> {
            if(c1.getName().endsWith("cbc")) {
                return 1;
            }
            if(c2.getName().endsWith("cbc")) {
                return -1;
            }
            return 0;

        }).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SFTPListService(this);
        }
        if(type == Find.class) {
            return (T) new SFTPFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new SFTPAttributesFinderFeature(this);
        }
        if(type == Read.class) {
            return (T) new SFTPReadFeature(this);
        }
        if(type == Upload.class) {
            return (T) new SFTPUploadFeature(this);
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
            return (T) new CustomOriginCloudFrontDistributionConfiguration(host, trust, key);
        }
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new TildeResolvingHomeFeature(host, new SFTPHomeDirectoryService(this)));
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
            log.warn("Disconnected {}", reason);
            failure = new SSHException(reason, message);
        }

        /**
         * @return Last disconnect reason
         */
        public SSHException getFailure() {
            return failure;
        }
    }

    private static final class LoggingSFTPEngine extends SFTPEngine {
        private final TranscriptListener transcript;

        public LoggingSFTPEngine(final SSHClient client, final TranscriptListener transcript) throws SSHException {
            super(client, String.valueOf(Path.DELIMITER));
            this.transcript = transcript;
        }

        @Override
        public Promise<Response, SFTPException> request(final Request req) throws IOException {
            transcript.log(Type.request, String.format("%d %s", req.getRequestID(), req.getType()));
            return super.request(req);
        }
    }
}
