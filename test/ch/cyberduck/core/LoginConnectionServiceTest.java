package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.ftp.FTPClient;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.junit.Test;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class LoginConnectionServiceTest extends AbstractTestCase {

    @Test(expected = BackgroundException.class)
    public void testCheckUnknown() throws Exception {
        final FTPSession session = new FTPSession(new Host("unknownhost.local"));
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginController(), new HostKeyCallback() {
            @Override
            public boolean verify(final String hostname, final int port, final PublicKey key) throws ConnectionCanceledException {
                assertEquals(Session.State.opening, session.getState());
                return true;
            }
        },
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                },
                new DisabledTranscriptListener());
        try {
            s.check(session, Cache.<Path>empty());
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
        final DAVSession session = new DAVSession(new Host(new DAVSSLProtocol(), "54.228.253.92", new Credentials("user", "p")),
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
                )
        );
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                }, new DisabledTranscriptListener());
        s.check(session, Cache.<Path>empty());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                }, new DisabledTranscriptListener());
        s.check(new FTPSession(new Host("")), Cache.<Path>empty());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testCheckReconnect() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                }, new DisabledTranscriptListener());
        final AtomicBoolean connected = new AtomicBoolean();
        final AtomicBoolean disconnected = new AtomicBoolean();
        try {
            final FTPSession session = new FTPSession(new Host("")) {
                @Override
                public void interrupt() throws BackgroundException {
                    disconnected.set(true);
                    super.interrupt();
                }

                @Override
                public FTPClient connect(final HostKeyCallback key, final TranscriptListener transcript) throws BackgroundException {
                    connected.set(true);
                    return null;
                }

                @Override
                public boolean isConnected() {
                    // Previously connected
                    return true;
                }
            };
            s.check(session, Cache.<Path>empty(), new BackgroundException("m", new SocketException("m")));
        }
        finally {
            assertTrue(disconnected.get());
            assertTrue(connected.get());
        }
    }
}
