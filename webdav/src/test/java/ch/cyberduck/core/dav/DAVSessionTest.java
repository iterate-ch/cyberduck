package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Category(IntegrationTest.class)
public class DAVSessionTest {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore()));
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final AttributedList<Path> list = session.list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotNull(list.get(new Path("/trunk", EnumSet.of(Path.Type.directory))));
        assertNotNull(list.get(new Path("/branches", EnumSet.of(Path.Type.directory))));
        assertNotNull(list.get(new Path("/tags", EnumSet.of(Path.Type.directory))));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectRefused() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "localhost", 2121);
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore()));
        try {
            session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Connection failed", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxyFailure() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore()),
                new ProxySocketFactory(host.getProtocol(), new DefaultTrustManagerHostnameCallback(host),
                        new DefaultSocketConfigurator(), new ProxyFinder() {
                    @Override
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 1111);
                    }
                })
        );
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        c.connect(session, PathCache.empty());
    }

    @Test
    @Ignore
    public void testConnectHttpProxy() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore()),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                    }
                }
        );
        final AtomicBoolean proxied = new AtomicBoolean();
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                if(request) {
                    if(message.contains("CONNECT")) {
                        proxied.set(true);
                    }
                }
            }
        });
        c.connect(session, PathCache.empty());
        assertTrue(proxied.get());
        assertTrue(session.isConnected());
        session.close();
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxyConnectionFailure() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore()),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 5555);
                    }
                }
        );
        final AtomicBoolean proxied = new AtomicBoolean();
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                if(request) {
                    if(message.contains("CONNECT")) {
                        proxied.set(true);
                    }
                }
            }
        });
        c.connect(session, PathCache.empty());
        assertFalse(proxied.get());
        assertFalse(session.isConnected());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch");
        final DAVSession session = new DAVSession(host);
        assertFalse(session.alert(new DisabledConnectionCallback()));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isSecured());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(BackgroundException e) {
            assertEquals("Method Not Allowed. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testHtmlResponse() throws Exception {
        final Host host = new Host(new DAVProtocol(), "media.cyberduck.ch");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        try {
            session.list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener());
        }
        catch(InteroperabilityException e) {
            assertEquals("Not a valid DAV response.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect301() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect302() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect303() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testRedirectGone() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test
    public void testLoginBasicAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(test);
        assertTrue(session.getFeature(Find.class).find(test));
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(session.getFeature(Find.class).find(test));
        session.close();
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertNotNull(session.list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveEnabled() throws Exception {
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", true);
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p")));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.alert(new DisabledConnectionCallback()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveEnabledSSL() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials("u", "p"));
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", true);
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertFalse(session.alert(new DisabledConnectionCallback()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveDisabled() throws Exception {
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", false);
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p")));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertFalse(session.alert(new DisabledConnectionCallback()));
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureBasicAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new TranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                if(request) {
                    if(message.contains("Authorization: Digest")) {
                        fail(message);
                    }
                }
            }
        });
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureDigestAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/digest");
        final DAVSession session = new DAVSession(host);
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", false);
        session.open(new DisabledHostKeyCallback(), new TranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                if(request) {
                    if(message.contains("Authorization: Basic")) {
                        fail(message);
                    }
                }
            }
        });
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason,
                               final LoginOptions options) throws LoginCanceledException {
                assertEquals(host.getCredentials(), credentials);
                assertEquals("Login failed", title);
                assertEquals("Authorization Required.", reason);
                assertFalse(options.publickey);
                throw new LoginCanceledException();
            }
        }, null);
    }

    @Test
    public void testFeatures() throws Exception {
        final Session session = new DAVSession(new Host(new DAVProtocol(), "h"));
        assertNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Headers.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
    }

    @Test(expected = LoginFailureException.class)
    public void testInteroperabilityDocumentsEpflTLSv1() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "documents.epfl.ch");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new CustomTrustSSLProtocolSocketFactory(new DisabledX509TrustManager(), new DefaultX509KeyManager(), "TLSv1"));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testInteroperabilityDocumentsEpflFailure() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "documents.epfl.ch");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new CustomTrustSSLProtocolSocketFactory(new DisabledX509TrustManager(), new DefaultX509KeyManager(), "TLSv12"));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    public void testLoginChangeUsername() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService c = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials,
                               String title, String reason, LoginOptions options) throws LoginCanceledException {
                if(prompt.get()) {
                    fail();
                }
                credentials.setUsername(System.getProperties().getProperty("webdav.user"));
                credentials.setPassword(System.getProperties().getProperty("webdav.password"));
                prompt.set(true);
            }

            @Override
            public void warn(Protocol protocol, String title, String message,
                             String continueButton, String disconnectButton, String preference) throws LoginCanceledException {
                //
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        c.connect(session, PathCache.empty());
        assertTrue(prompt.get());
        assertTrue(session.isConnected());
        assertFalse(session.isSecured());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    @Ignore
    public void testMutualTlsUnknownCA() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "auth.startssl.com");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, String hostname, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("auth.startssl.com", hostname);
                        assertEquals("The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to auth.startssl.com.",
                                prompt);
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Certification Authority, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 1 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 2 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        assertTrue(Arrays.asList(issuers).contains(new X500Principal("" +
                                "CN=StartCom Class 3 Primary Intermediate Client CA, OU=Secure Digital Certificate Signing, O=StartCom Ltd., C=IL")));
                        throw new ConnectionCanceledException(prompt);
                    }
                }));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public void prompt(Host bookmark, Credentials credentials,
                                       String title, String reason, LoginOptions options) throws LoginCanceledException {
                        //
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            c.connect(session, PathCache.empty());
        }
        catch(InteroperabilityException e) {
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testConnectMutualTlsNoCertificate() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/dav");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, String hostname, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("test.cyberduck.ch", hostname);
                        assertEquals("The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to test.cyberduck.ch.",
                                prompt);
                        throw new ConnectionCanceledException(prompt);
                    }
                }));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        try {
            c.connect(session, PathCache.empty());
        }
        catch(InteroperabilityException e) {
            assertEquals("Handshake failure. Unable to negotiate an acceptable set of security parameters. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testConnectMutualTls() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/dav");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager() {
            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                assertEquals(2, certs.length);
                super.verify(hostname, certs, cipher);
            }
        },
                new KeychainX509KeyManager(new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, String hostname, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("test.cyberduck.ch", hostname);
                        assertEquals("The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to test.cyberduck.ch.",
                                prompt);
                        throw new ConnectionCanceledException(prompt);
                    }
                }));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        try {
            c.connect(session, PathCache.empty());
        }
        catch(InteroperabilityException e) {
            assertEquals("Handshake failure. Unable to negotiate an acceptable set of security parameters. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testTrustChain2() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final AtomicBoolean verified = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager() {
            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                assertEquals(3, certs.length);
                assertEquals("CN=StartCom Class 2 Primary Intermediate Server CA,OU=Secure Digital Certificate Signing,O=StartCom Ltd.,C=IL",
                        certs[1].getSubjectX500Principal().getName());
                assertEquals("C=CH,ST=Bern,L=Bern,O=iterate GmbH,CN=*.cyberduck.io,E=hostmaster@cyberduck.io",
                        certs[0].getSubjectDN().getName());
                verified.set(true);
                super.verify(hostname, certs, cipher);
            }
        },
                new KeychainX509KeyManager(new DisabledCertificateStore()));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        c.connect(session, PathCache.empty());
        assertTrue(verified.get());
        session.close();
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        final Session session = new DAVSession(new Host(new DAVSSLProtocol(), "54.228.253.92", new Credentials("user", "p")),
                new CertificateStoreX509TrustManager(new TrustManagerHostnameCallback() {
                    @Override
                    public String getTarget() {
                        return "54.228.253.92";
                    }
                }, new DisabledCertificateStore() {
                    @Override
                    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) {
                        return false;
                    }
                }
                ), new DefaultX509KeyManager()
        );
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        s.check(session, PathCache.empty());
    }

    @Test
    public void testRedirectHttpsAlert() throws Exception {
        final Host host = new Host(new DAVProtocol(), "svn.cyberduck.io");
        final AtomicBoolean warning = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public void warn(final Protocol protocol, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                        assertEquals("Unsecured WebDAV (HTTP) connection", title);
                        assertEquals("connection.unsecure.svn.cyberduck.io", preference);
                        warning.set(true);
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        c.connect(session, PathCache.empty());
        assertTrue(warning.get());
        session.close();
    }
}
