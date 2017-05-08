package ch.cyberduck.core.ftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPSessionTest {

    @Test
    public void testConnectAnonymous() throws Exception {
        final Host host = new Host(new FTPProtocol(), "mirror.switch.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        assertEquals(Session.State.closed, session.getState());
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener());
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertEquals(Session.State.open, session.getState());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertNotNull(new FTPWorkdirService(session).find());
        session.close();
        assertEquals(Session.State.closed, session.getState());
        assertFalse(session.isConnected());
    }

    @Ignore
    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxyForbiddenHttpResponse() throws Exception {
        final Host host = new Host(new FTPProtocol(), "mirror.switch.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host, new ProxySocketFactory(host.getProtocol(), new DefaultTrustManagerHostnameCallback(host),
                new DefaultSocketConfigurator(), new ProxyFinder() {
            @Override
            public Proxy find(final Host target) {
                return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
            }
        })
        );
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener());
        try {
            c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Invalid response HTTP/1.1 403 Forbidden from HTTP proxy localhost. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", e.getDetail());
            throw e;
        }
        assertTrue(session.isConnected());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectTLSNotSupported() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "mirror.switch.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
//        assertFalse(session.isSecured());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path path = new FTPWorkdirService(session).find();
        assertNotNull(path);
        assertEquals(path, new FTPWorkdirService(session).find());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = BackgroundException.class)
    public void testWorkdir() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        new FTPWorkdirService(session).find();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
    }

    @Test(expected = NotfoundException.class)
    public void testNotfound() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        session.list(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testConnectionTlsUpgrade() throws Exception {
        final Host host = new Host(new FTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host) {
            @Override
            public void login(final HostPasswordStore keychain, final LoginCallback login, CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
                assertEquals(Session.State.open, this.getState());
                super.login(keychain, login, cancel, cache);
                assertEquals(new FTPTLSProtocol(), host.getProtocol());
            }
        };
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertEquals(new FTPProtocol(), host.getProtocol());
        final AtomicBoolean warned = new AtomicBoolean();
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message, final String continueButton,
                             final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                // Cancel to switch
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        l.authenticate(session, PathCache.empty(), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new DisabledCancelCallback());
        assertEquals(new FTPTLSProtocol(), host.getProtocol());
        assertTrue(warned.get());
    }

    @Test
    public void testFeatures() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.getFeature(DistributionConfiguration.class));
        assertNull(session.getFeature(UnixPermission.class));
        assertNull(session.getFeature(Timestamp.class));
        session.open(new DisabledHostKeyCallback());
        assertNull(session.getFeature(UnixPermission.class));
        assertNull(session.getFeature(Timestamp.class));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertNotNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        session.close();
    }

    @Test
    public void testCloseFailure() throws Exception {
        final Host host = new Host(new FTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final BackgroundException failure = new BackgroundException(new FTPException(500, "f"));
        final FTPSession session = new FTPSession(host) {
            @Override
            protected void logout() throws BackgroundException {
                throw failure;
            }
        };
        session.open(new DisabledHostKeyCallback());
        assertEquals(Session.State.open, session.getState());
        try {
            session.close();
            fail();
        }
        catch(BackgroundException e) {
            assertEquals(failure, e);
        }
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    @Ignore
    public void testConnectMutualTls() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final AtomicBoolean callback = new AtomicBoolean();
        final FTPSession session = new FTPSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(host, new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, Host bookmark, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("test.cyberduck.ch", bookmark);
                        assertEquals("The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to test.cyberduck.ch.",
                                prompt);
                        callback.set(true);
                        throw new ConnectionCanceledException(prompt);
                    }
                }));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener());
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(callback.get());
        session.close();
    }
}
