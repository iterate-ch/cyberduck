package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVSessionTest extends AbstractTestCase {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        final AttributedList<Path> list = session.list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotNull(list.get(new Path("/trunk", EnumSet.of(Path.Type.directory)).getReference()));
        assertNotNull(list.get(new Path("/branches", EnumSet.of(Path.Type.directory)).getReference()));
        assertNotNull(list.get(new Path("/tags", EnumSet.of(Path.Type.directory)).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = InteroperabilityException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertFalse(session.alert());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isSecured());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        }
        catch(BackgroundException e) {
            assertEquals("Method Not Allowed. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testHtmlResponse() throws Exception {
        final Host host = new Host(new DAVProtocol(), "media.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        try {
            session.list(session.workdir(), new DisabledListProgressListener());
        }
        catch(InteroperabilityException e) {
            assertEquals("Not a valid DAV response.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect301() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect302() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect303() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testRedirectGone() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
    }

    @Test
    public void testLoginBasicAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(test);
        assertTrue(session.getFeature(Find.class).find(test));
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        assertFalse(session.getFeature(Find.class).find(test));
        session.close();
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertNotNull(session.list(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener()));
        session.close();
    }

    @Test
    public void testAlert() throws Exception {
        Preferences.instance().setProperty("webdav.basic.preemptive", true);
        assertTrue(new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p"))).alert());
        assertFalse(new DAVSession(new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials("u", "p"))).alert());
        Preferences.instance().setProperty("webdav.basic.preemptive", false);
        assertFalse(new DAVSession(new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials("u", "p"))).alert());
        assertFalse(new DAVSession(new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials("u", "p"))).alert());
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
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureDigestAuth() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/digest");
        final DAVSession session = new DAVSession(host);
        Preferences.instance().setProperty("webdav.basic.preemptive", false);
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
        session.login(new DisabledPasswordStore(), new DisabledLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                               final LoginOptions options) throws LoginCanceledException {
                assertEquals(host.getCredentials(), credentials);
                assertEquals("Login failed", title);
                assertEquals("Authorization Required.", reason);
                assertFalse(options.publickey);
                throw new LoginCanceledException();
            }
        }, null, new DisabledTranscriptListener());
    }

    @Test(expected = LoginFailureException.class)
    @Ignore
    public void testLoginErrorBasicFallback() throws Exception {
        final Host host = new Host(new DAVProtocol(), "prod.lattusdemo.com", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/namespace");
        final DAVSession session = new DAVSession(host);
        Preferences.instance().setProperty("webdav.basic.preemptive", true);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        }
        catch(LoginFailureException e) {
            assertEquals("Unauthorized. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFeatures() throws Exception {
        final Session session = new DAVSession(new Host("h"));
        assertNull(session.getFeature(UnixPermission.class));
        assertNull(session.getFeature(Timestamp.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Headers.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
    }

    @Test
    public void testdavpiximegallery() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "g2.pixi.me", new Credentials(
                "webdav", "webdav"
        ));
        host.setDefaultPath("/w/webdav/");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    @Test
    public void testdavpixime() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "dav.pixi.me", new Credentials(
                "webdav", "webdav"
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    @Test
    public void testtlsv11pixime() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "tlsv11.pixi.me", new Credentials(
                "webdav", "webdav"
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    @Test
    public void testtlsv12pixime() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "tlsv12.pixi.me", new Credentials(
                "webdav", "webdav"
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertTrue(session.isConnected());
        assertTrue(session.isSecured());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertNotNull(session.workdir());
        assertFalse(session.getAcceptedIssuers().isEmpty());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testUnrecognizedName() throws Exception {
        final DAVSession session = new DAVSession(new Host(new DAVSSLProtocol(), "sds-security.selfhost.eu", 8000));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
    }

    @Test
    public void testLoginChangeUsername() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"),
                Preferences.instance().getProperty("connection.login.anon.pass"))
        );
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService c = new LoginConnectionService(new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials,
                               String title, String reason, LoginOptions options) throws LoginCanceledException {
                if(prompt.get()) {
                    fail();
                }
                credentials.setUsername(properties.getProperty("webdav.user"));
                credentials.setPassword(properties.getProperty("webdav.password"));
                prompt.set(true);
            }

            @Override
            public void warn(Protocol protocol, String title, String message,
                             String continueButton, String disconnectButton, String preference) throws LoginCanceledException {
                //
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        c.connect(session, Cache.<Path>empty());
        assertTrue(prompt.get());
        assertTrue(session.isConnected());
        assertFalse(session.isSecured());
        assertNotNull(session.workdir());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testMutualTlsUnknownCA() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "auth.startssl.com");
        final TrustManagerHostnameCallback callback = new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "auth.startssl.com";
            }
        };
        final DAVSession session = new DAVSession(host, new KeychainX509TrustManager(callback),
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
                new DisabledLoginController() {
                    @Override
                    public void prompt(Protocol protocol, Credentials credentials,
                                       String title, String reason, LoginOptions options) throws LoginCanceledException {
                        //
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        c.connect(session, Cache.<Path>empty());
    }

    @Test(expected = InteroperabilityException.class)
    public void testConnectMutualTlsNoCertificate() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"),
                Preferences.instance().getProperty("connection.login.anon.pass"))
        );
        host.setDefaultPath("/dav");
        final DAVSession session = new DAVSession(host, new KeychainX509TrustManager(new DisabledX509HostnameVerifier()),
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
                new DisabledLoginController(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            c.connect(session, Cache.<Path>empty());
        }
        catch(InteroperabilityException e) {
            assertEquals("Handshake failure. Unable to negotiate an acceptable set of security parameters. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testConnectMutualTls() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"),
                Preferences.instance().getProperty("connection.login.anon.pass"))
        );
        host.setDefaultPath("/dav");
        final DAVSession session = new DAVSession(host, new KeychainX509TrustManager(new DisabledX509HostnameVerifier()),
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
                new DisabledLoginController(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            c.connect(session, Cache.<Path>empty());
        }
        catch(InteroperabilityException e) {
            assertEquals("Handshake failure. Unable to negotiate an acceptable set of security parameters. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testConnectProxy() throws Exception {

    }

    @Test
    public void testConnectProxyAuth() throws Exception {

    }
}
