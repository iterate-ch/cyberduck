package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.SSHClient;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class LoginConnectionServiceTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testNoResolveForHTTPProxy() throws Exception {
        final Session session = new S3Session(new Host(new S3Protocol(), "unknownhost.local", new Credentials("user", "")));
        final LoginConnectionService s = new LoginConnectionService(new DisabledProxyFinder() {
            @Override
            public Proxy find(final Host target) {
                return new Proxy(Proxy.Type.HTTP, "proxy.local", 6666);
            }
        }, new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        s.check(session, PathCache.empty());
    }

    @Test(expected = BackgroundException.class)
    public void testConnectDnsFailure() throws Exception {
        final Session session = new FTPSession(new Host(new FTPProtocol(), "unknownhost.local", new Credentials("user", "")));
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new HostKeyCallback() {
            @Override
            public boolean verify(final String hostname, final int port, final PublicKey key) throws ConnectionCanceledException {
                assertEquals(Session.State.opening, session.getState());
                return true;
            }
        }, new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new DisabledTranscriptListener());
        try {
            s.check(session, PathCache.empty());
        }
        catch(BackgroundException e) {
            assertEquals("Connection failed", e.getMessage());
            assertEquals("DNS lookup for unknownhost.local failed. DNS is the network service that translates a server name to its Internet address. This error is most often caused by having no connection to the Internet or a misconfigured network. It can also be caused by an unresponsive DNS server or a firewall preventing access to the network.", e.getDetail());
            assertEquals(UnknownHostException.class, e.getCause().getClass());
            assertEquals(Session.State.closed, session.getState());
            throw e;
        }
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

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        s.check(new FTPSession(new Host("")), PathCache.empty());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testCheckReconnect() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        final AtomicBoolean disconnected = new AtomicBoolean();
        try {
            final Session session = new FTPSession(new Host(new FTPProtocol(), "", new Credentials("user", ""))) {
                @Override
                public void interrupt() throws BackgroundException {
                    disconnected.set(true);
                    super.interrupt();
                }
            };
            s.check(session, PathCache.empty(), new BackgroundException("m", new SocketException("m")));
        }
        finally {
            assertTrue(disconnected.get());
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testPasswordChange() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean();
        final AtomicBoolean keychain = new AtomicBoolean();
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                // New password entered
                credentials.setPassword("b");
                prompt.set(true);
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                keychain.set(true);
                // Old password stored
                return "a";
            }
        }, new DisabledProgressListener(), new DisabledTranscriptListener());
        final Host host = new Host(new SFTPProtocol(), "localhost", new Credentials("user", ""));
        final Session session = new SFTPSession(host) {
            @Override
            public SSHClient connect(final HostKeyCallback key) throws BackgroundException {
                connected.set(true);
                return null;
            }

            @Override
            public boolean isConnected() {
                return connected.get();
            }

            @Override
            public void login(final PasswordStore p, final LoginCallback l, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
                if(prompt.get()) {
                    assertEquals("b", host.getCredentials().getPassword());
                    throw new LoginCanceledException();
                }
                if(keychain.get()) {
                    assertFalse(prompt.get());
                    assertEquals("a", host.getCredentials().getPassword());
                    throw new LoginFailureException("f");
                }
            }
        };
        try {
            s.check(session, PathCache.empty());
        }
        finally {
            assertTrue(keychain.get());
            assertTrue(prompt.get());
        }
    }
}
