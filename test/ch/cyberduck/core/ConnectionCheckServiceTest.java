package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class ConnectionCheckServiceTest extends AbstractTestCase {

    @Test(expected = BackgroundException.class)
    public void testCheckUnknown() throws Exception {
        final FTPSession session = new FTPSession(new Host("unknownhost.local"));
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new HostKeyController() {
            @Override
            public boolean verify(final String hostname, final int port, final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws IOException, ConnectionCanceledException {
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
                }
        );
        try {
            s.check(session);
        }
        catch(BackgroundException e) {
            assertEquals(UnknownHostException.class, e.getCause().getClass());
            assertEquals(Session.State.closed, session.getState());
            throw e;
        }
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        CertificateStoreFactory.addFactory(Factory.NATIVE_PLATFORM, new CertificateStoreFactory() {
            @Override
            protected CertificateStore create() {
                return new DisabledCertificateStore() {
                    @Override
                    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) {
                        return false;
                    }
                };
            }
        });
        final DAVSession session = new DAVSession(new Host(Protocol.WEBDAV_SSL, "54.228.253.92", new Credentials("user", "p")));
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                });
        s.check(session);
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(),
                new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }
                });
        s.check(new FTPSession(new Host("")));
    }
}
