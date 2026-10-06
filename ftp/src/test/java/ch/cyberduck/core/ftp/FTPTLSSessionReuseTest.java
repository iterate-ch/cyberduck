package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Verify the data connection resumes the TLS session of the control connection. Many servers reject data connections
 * not reusing the session of the control connection (<tt>require_ssl_reuse</tt> in vsftpd, <tt>mod_tls</tt> without
 * <tt>NoSessionReuseRequired</tt> in ProFTPD).
 * <p>
 * Resumption is asserted with the number of certificate validations in the client. A resumed handshake, both the
 * abbreviated handshake in TLS 1.2 and PSK resumption in TLS 1.3, does not send a certificate message. A single
 * validation for the control connection therefore means every data connection has resumed its session.
 *
 * @see FTPClient#_prepareDataSocket_(Socket)
 */
@RunWith(Parameterized.class)
public class FTPTLSSessionReuseTest {

    private static final String PASSWORD = "changeit";

    @Parameterized.Parameters(name = "protocol = {0}")
    public static Object[] data() {
        return new Object[]{"TLSv1.2", "TLSv1.3"};
    }

    @Parameterized.Parameter
    public String protocol;

    private final int port = ThreadLocalRandom.current().nextInt(2000, 3000);

    /**
     * Number of certificate validations in the client, one per full handshake
     */
    private final AtomicInteger handshakes = new AtomicInteger();

    /**
     * Data connections accepted by the server
     */
    private final List<SSLSocket> sockets = Collections.synchronizedList(new ArrayList<>());

    private FtpServer server;
    private Local directory;
    private FTPSession session;
    private String protocols;

    @Before
    public void start() throws Exception {
        final SslConfiguration ssl = new RecordingSslConfiguration(this.context(), protocol);
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final UserManager users = new PropertiesUserManagerFactory().createUserManager();
        final BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("test");
        directory = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        directory.mkdir();
        user.setHomeDirectory(directory.getAbsolute());
        final List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        users.save(user);
        serverFactory.setUserManager(users);
        final DataConnectionConfigurationFactory data = new DataConnectionConfigurationFactory();
        data.setImplicitSsl(false);
        data.setSslConfiguration(ssl);
        final ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);
        factory.setImplicitSsl(false);
        factory.setSslConfiguration(ssl);
        factory.setDataConnectionConfiguration(data.createDataConnectionConfiguration());
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        server.start();
    }

    @After
    public void stop() {
        if(null != session) {
            try {
                session.close();
            }
            catch(BackgroundException e) {
                // Ignore
            }
        }
        if(null != protocols) {
            PreferencesFactory.get().setProperty("connection.ssl.protocols", protocols);
        }
        if(null != server) {
            server.stop();
        }
        if(null != directory) {
            try {
                directory.delete();
            }
            catch(AccessDeniedException | NotfoundException e) {
                // Ignore
            }
        }
    }

    @Test
    public void testSessionReuseSingleDataConnection() throws Exception {
        final FTPSession session = this.connect();
        this.list(session);
        assertEquals(protocol, this.negotiated());
        assertEquals(String.format("Full handshake for data connection with %s instead of resuming session of control connection", protocol),
                1, handshakes.get());
    }

    @Test
    public void testSessionReuseMultipleDataConnections() throws Exception {
        final FTPSession session = this.connect();
        for(int i = 0; i < 5; i++) {
            this.list(session);
        }
        assertEquals(protocol, this.negotiated());
        assertEquals(String.format("Full handshake for data connection with %s instead of resuming session of control connection", protocol),
                1, handshakes.get());
    }

    private FTPSession connect() throws BackgroundException {
        final Preferences preferences = PreferencesFactory.get();
        protocols = preferences.getProperty("connection.ssl.protocols");
        preferences.setProperty("connection.ssl.protocols", protocol);
        final Host host = new Host(new FTPTLSProtocol(), "localhost", port, new Credentials("test", "test")) {
            @Override
            public String getProperty(final String key) {
                if(key.equals("ftp.datachannel.epsv")) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        session = new FTPSession(host, new CountingX509TrustManager(handshakes), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton,
                             final String disconnectButton, final String preference) {
                fail(message);
            }
        }, HostKeyCallback.noop, new DisabledPasswordStore(), ProgressListener.noop).check(session, CancelCallback.noop);
        // Full handshake for control connection only
        assertEquals(1, handshakes.get());
        return session;
    }

    private void list(final FTPSession session) throws BackgroundException {
        assertNotNull(session.getFeature(ListService.class).list(
                session.getFeature(Home.class).find(), new DisabledListProgressListener()));
    }

    /**
     * @return Protocol version negotiated for the data connections
     */
    private String negotiated() {
        assertEquals(1L, sockets.stream().map(socket -> socket.getSession().getProtocol()).distinct().count());
        return sockets.get(0).getSession().getProtocol();
    }

    /**
     * @return Context with self signed certificate for <tt>localhost</tt>
     */
    private SSLContext context() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair pair = generator.generateKeyPair();
        final X500Name name = new X500Name("CN=localhost");
        final X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(
                new JcaX509v3CertificateBuilder(name, BigInteger.ONE,
                        Date.from(Instant.now().minus(Duration.ofDays(1))), Date.from(Instant.now().plus(Duration.ofDays(1))),
                        name, pair.getPublic()).build(new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate())));
        final KeyStore store = KeyStore.getInstance("JKS");
        store.load(null, PASSWORD.toCharArray());
        store.setKeyEntry("localhost", pair.getPrivate(), PASSWORD.toCharArray(), new Certificate[]{certificate});
        final KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
        factory.init(store, PASSWORD.toCharArray());
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(factory.getKeyManagers(), new TrustManager[]{new DisabledX509TrustManager()}, null);
        return context;
    }

    /**
     * Count certificate validations in the client. Only a full handshake sends a certificate message.
     */
    private static final class CountingX509TrustManager extends DisabledX509TrustManager {
        private final AtomicInteger count;

        public CountingX509TrustManager(final AtomicInteger count) {
            this.count = count;
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] certs, final String cipher) {
            count.incrementAndGet();
            super.checkServerTrusted(certs, cipher);
        }
    }

    /**
     * Single context for both control and data connection to share the session cache of the server
     */
    private final class RecordingSslConfiguration implements SslConfiguration {
        private final SSLContext context;
        private final String protocol;

        public RecordingSslConfiguration(final SSLContext context, final String protocol) {
            this.context = context;
            this.protocol = protocol;
        }

        @Override
        public SSLSocketFactory getSocketFactory() {
            return new RecordingSSLSocketFactory(context.getSocketFactory(), sockets);
        }

        @Override
        public SSLContext getSSLContext() {
            return context;
        }

        @Override
        public SSLContext getSSLContext(final String protocol) {
            return context;
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return null;
        }

        @Override
        public String getEnabledProtocol() {
            return protocol;
        }

        @Override
        public String[] getEnabledProtocols() {
            return new String[]{protocol};
        }

        @Override
        public ClientAuth getClientAuth() {
            return ClientAuth.NONE;
        }
    }

    /**
     * Keep reference to accepted data connections to determine the negotiated protocol version
     */
    private static final class RecordingSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory proxy;
        private final List<SSLSocket> sockets;

        public RecordingSSLSocketFactory(final SSLSocketFactory proxy, final List<SSLSocket> sockets) {
            this.proxy = proxy;
            this.sockets = sockets;
        }

        private Socket record(final Socket socket) {
            if(socket instanceof SSLSocket) {
                sockets.add((SSLSocket) socket);
            }
            return socket;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return proxy.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return proxy.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket() throws IOException {
            return this.record(proxy.createSocket());
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException {
            return this.record(proxy.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            return this.record(proxy.createSocket(host, port));
        }

        @Override
        public Socket createSocket(final String host, final int port, final InetAddress address, final int localPort) throws IOException {
            return this.record(proxy.createSocket(host, port, address, localPort));
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port) throws IOException {
            return this.record(proxy.createSocket(host, port));
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port, final InetAddress address, final int localPort) throws IOException {
            return this.record(proxy.createSocket(host, port, address, localPort));
        }
    }
}
