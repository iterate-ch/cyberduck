package ch.cyberduck.core.ftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.ftp.list.FTPListService;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultVersioningFeature;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPSessionTest extends AbstractFTPTest {

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.get().register(new FTPTLSProtocol() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        });
    }

    @Ignore
    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxyForbiddenHttpResponse() throws Exception {
        final Host host = new Host(new FTPProtocol(), "mirror.switch.ch", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        final LoginConnectionService c = new LoginConnectionService(
            new DisabledLoginCallback(),
            new DisabledHostKeyCallback(),
            new DisabledPasswordStore(),
            new DisabledProgressListener(),
            new ProxyFinder() {
                @Override
                public Proxy find(final String target) {
                    return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                }
            });
        try {
            c.connect(session, new DisabledCancelCallback());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Invalid response HTTP/1.1 403 Forbidden from HTTP proxy localhost. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", e.getDetail());
            throw e;
        }
        assertTrue(session.isConnected());
    }

    @Test
    public void testConnect() throws Exception {
        final Path path = new FTPWorkdirService(session).find();
        assertNotNull(path);
        assertEquals(path, new FTPWorkdirService(session).find());
        assertTrue(session.isConnected());
    }

    @Test
    public void testTouch() throws Exception {
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
    }

    @Test(expected = NotfoundException.class)
    public void testNotfound() throws Exception {
        new FTPListService(session, null, TimeZone.getDefault()).list(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testFeatures() {
        assertNotNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        assertEquals(DefaultVersioningFeature.class, session.getFeature(Versioning.class).getClass());
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
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
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
            new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore() {
                @Override
                public X509Certificate choose(final CertificateIdentityCallback prompt, final String[] keyTypes, final Principal[] issuers, final Host bookmark) throws ConnectionCanceledException {
                    assertEquals("test.cyberduck.ch", bookmark.getHostname());
                    callback.set(true);
                    throw new ConnectionCanceledException();
                }
            }));
        final LoginConnectionService c = new LoginConnectionService(
            new DisabledLoginCallback(),
            new DisabledHostKeyCallback(),
            new DisabledPasswordStore(),
            new DisabledProgressListener());
        c.connect(session, new DisabledCancelCallback());
        assertTrue(callback.get());
    }
}
