package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.ProxyLoginFailureException;
import ch.cyberduck.core.exception.SSLNegotiateException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultVersioningFeature;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.security.auth.x500.X500Principal;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVSessionTest extends AbstractDAVTest {

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectRefused() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "localhost", 2121);
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DefaultCertificateStore()));
        try {
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Connection failed", e.getMessage());
            throw e;
        }
    }

    @Test(expected = BackgroundException.class)
    public void testHtmlResponse() throws Exception {
        final Host host = new Host(new DAVProtocol(), "cyberduck.ch");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        try {
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
            new DAVListService(session).list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener());
        }
        catch(InteroperabilityException | ConflictException e) {
            assertEquals("Unexpected response (405 Method Not Allowed). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testLoginBasicAuth() {
    }

    @Test
    public void testLoginNTLM() throws Exception {
        final Host host = new Host(new DAVProtocol(), "winbuild.iterate.ch", new Credentials(
                PROPERTIES.get("webdav.iis.user"), PROPERTIES.get("webdav.iis.password")
        ));
        host.setDefaultPath("/WebDAV");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());;
        assertTrue(session.getFeature(Find.class).find(test));
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
    }

    @Test
    public void testFeatures() {
        final Session session = new DAVSession(new Host(new DAVProtocol(), "h"), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertEquals(DefaultVersioningFeature.class, session.getFeature(Versioning.class).getClass());
        assertNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Headers.class));
        assertNull(session.getFeature(DistributionConfiguration.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test(expected = SSLNegotiateException.class)
    @Ignore
    public void testMutualTlsUnknownCA() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "auth.startssl.com");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(final CertificateIdentityCallback prompt, final String[] keyTypes, final Principal[] issuers, final Host bookmark) throws ConnectionCanceledException {
                        assertEquals("auth.startssl.com", bookmark.getHostname());
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Certification Authority, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 1 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 2 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 3 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        throw new ConnectionCanceledException();
                    }
                }));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, String username,
                                              String title, String reason, LoginOptions options) {
                        //
                        return new Credentials();
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener());
        c.connect(session, new DisabledCancelCallback());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        final Session session = new DAVSession(new Host(new DAVSSLProtocol(), "54.228.253.92", new Credentials("user", "p")),
                new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new TrustManagerHostnameCallback() {
                    @Override
                    public String getTarget() {
                        return "54.228.253.92";
                    }
                }, new DisabledCertificateStore() {
                    @Override
                    public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) {
                        return false;
                    }
                }
                ), new DefaultX509KeyManager()
        );
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener());
        s.check(session, new DisabledCancelCallback());
    }

    @Test
    @Ignore
    public void testRedirectHttpsAlert() throws Exception {
        final Host host = new Host(new DAVProtocol(), "svn.cyberduck.io");
        final AtomicBoolean warning = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                        assertEquals("Unsecured WebDAV (HTTP) connection", title);
                        assertEquals("connection.unsecure.svn.cyberduck.io", preference);
                        warning.set(true);
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()
        );
        c.connect(session, new DisabledCancelCallback());
        assertTrue(warning.get());
        session.close();
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testProxyNoConnect() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
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
                }
        );
        c.connect(session, new DisabledCancelCallback());
        session.close();
    }

    @Ignore
    @Test(expected = ProxyLoginFailureException.class)
    public void testConnectProxyInvalidCredentials() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                        return new Credentials("test", "n");
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final String target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                    }
                }
        );
        c.connect(session, new DisabledCancelCallback());
        session.close();
    }

    @Test
    @Ignore
    public void testConnectProxy() throws Throwable {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final AtomicBoolean verified = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager() {
            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                assertNotNull(hostname);
                verified.set(true);
                super.verify(hostname, certs, cipher);
            }
        },
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                        return new Credentials("test", "test");
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final String target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 8080);
                    }
                }
        );
        try {
            Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    c.connect(session, new DisabledCancelCallback());
                    return null;
                }
            }).get();
        }
        catch(ExecutionException e) {
            throw e.getCause();
        }
        finally {
            assertTrue(verified.get());
            session.close();
        }
    }

    @Test
    @Ignore
    public void testConnectProxyHttps() throws Throwable {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final AtomicBoolean verified = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager() {
            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                assertNotNull(hostname);
                verified.set(true);
                super.verify(hostname, certs, cipher);
            }
        },
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                        return new Credentials("test", "test");
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final String target) {
                        return new Proxy(Proxy.Type.HTTPS, "localhost", 8080);
                    }
                }
        );
        try {
            Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    c.connect(session, new DisabledCancelCallback());
                    return null;
                }
            }).get();
        }
        catch(ExecutionException e) {
            throw e.getCause();
        }
        finally {
            assertTrue(verified.get());
            session.close();
        }
    }
}
