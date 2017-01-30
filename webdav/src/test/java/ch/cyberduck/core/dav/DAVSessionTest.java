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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVSessionTest {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore(), host));
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
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
                new CertificateStoreX509KeyManager(new DefaultCertificateStore(), host));
        try {
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
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
                new CertificateStoreX509KeyManager(new DefaultCertificateStore(), host),
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
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
    }

    @Test
    @Ignore
    public void testConnectHttpProxy() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore(), host),
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
            public void log(final Type request, final String message) {
                switch(request) {
                    case request:
                    if(message.contains("CONNECT")) {
                        proxied.set(true);
                    }
                }
            }
        });
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(proxied.get());
        assertTrue(session.isConnected());
        session.close();
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxyConnectionFailure() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.io");
        final DAVSession session = new DAVSession(host,
                new CertificateStoreX509TrustManager(new DefaultTrustManagerHostnameCallback(host), new DefaultCertificateStore()),
                new CertificateStoreX509KeyManager(new DefaultCertificateStore(), host),
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
            public void log(final Type request, final String message) {
                switch(request) {
                    case request:
                        if(message.contains("CONNECT")) {
                            proxied.set(true);
                        }
                }
            }
        });
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertFalse(proxied.get());
        assertFalse(session.isConnected());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch");
        final DAVSession session = new DAVSession(host);
        assertFalse(session.alert(new DisabledConnectionCallback()));
        try {
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        }
        catch(BackgroundException e) {
            assertEquals("Unexpected response (405 Method Not Allowed). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testHtmlResponse() throws Exception {
        final Host host = new Host(new DAVProtocol(), "media.cyberduck.ch");
        final DAVSession session = new DAVSession(host);
        try {
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
            session.list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener());
        }
        catch(InteroperabilityException e) {
            assertEquals("Unexpected response (405 Method Not Allowed). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect301() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect302() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect303() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testRedirectGone() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
    }

    @Test
    public void testLoginBasicAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
        session.close();
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch");
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        assertNotNull(session.list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveEnabled() throws Exception {
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", true);
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p")));
        session.open(new DisabledHostKeyCallback());
        assertTrue(session.alert(new DisabledConnectionCallback()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveEnabledSSL() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials("u", "p"));
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", true);
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        assertFalse(session.alert(new DisabledConnectionCallback()));
        session.close();
    }

    @Test
    public void testAlertPreemptiveDisabled() throws Exception {
        PreferencesFactory.get().setProperty("webdav.basic.preemptive", false);
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p")));
        session.open(new DisabledHostKeyCallback());
        assertFalse(session.alert(new DisabledConnectionCallback()));
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureBasicAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
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
        session.open(new DisabledHostKeyCallback());
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
        }, null, PathCache.empty());
    }

    @Test
    public void testFeatures() throws Exception {
        final Session session = new DAVSession(new Host(new DAVProtocol(), "h"));
        assertNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Headers.class));
        assertNull(session.getFeature(DistributionConfiguration.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test(expected = LoginFailureException.class)
    public void testInteroperabilityDocumentsEpflTLSv1() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "documents.epfl.ch");
        final DisabledX509TrustManager trust = new DisabledX509TrustManager();
        final DAVSession session = new DAVSession(host, trust, new DefaultX509KeyManager(),
                new CustomTrustSSLProtocolSocketFactory(trust, new DefaultX509KeyManager(), "TLSv1"));
        session.open(new DisabledHostKeyCallback());
        assertTrue(session.isConnected());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertFalse(Arrays.asList(trust.getAcceptedIssuers()).isEmpty());
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
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(prompt.get());
        assertTrue(session.isConnected());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    @Ignore
    public void testMutualTlsUnknownCA() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "auth.startssl.com");
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(host, new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, Host bookmark, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("auth.startssl.com", bookmark.getHostname());
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
            c.connect(session, PathCache.empty(), new DisabledCancelCallback());
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
                new KeychainX509KeyManager(host, new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, Host bookmark, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("test.cyberduck.ch", bookmark.getHostname());
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
            c.connect(session, PathCache.empty(), new DisabledCancelCallback());
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
                new KeychainX509KeyManager(host, new DisabledCertificateStore() {
                    @Override
                    public X509Certificate choose(String[] keyTypes, Principal[] issuers, Host bookmark, String prompt)
                            throws ConnectionCanceledException {
                        assertEquals("test.cyberduck.ch", bookmark.getHostname());
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
            c.connect(session, PathCache.empty(), new DisabledCancelCallback());
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
                assertEquals(2, certs.length);
                assertEquals("CN=Let's Encrypt Authority X3,O=Let's Encrypt,C=US",
                        certs[1].getSubjectX500Principal().getName());
                assertEquals("CN=svn.cyberduck.io",
                        certs[0].getSubjectDN().getName());
                verified.set(true);
                super.verify(hostname, certs, cipher);
            }
        },
                new KeychainX509KeyManager(host, new DisabledCertificateStore()));
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
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
        s.check(session, PathCache.empty(), new DisabledCancelCallback());
    }

    @Test
    public void testRedirectHttpsAlert() throws Exception {
        final Host host = new Host(new DAVProtocol(), "svn.cyberduck.io");
        final AtomicBoolean warning = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(host, new DisabledCertificateStore())) {
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
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(warning.get());
        session.close();
    }
}
