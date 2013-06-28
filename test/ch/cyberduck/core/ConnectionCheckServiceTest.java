package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.Controller;

import org.junit.Test;

import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class ConnectionCheckServiceTest extends AbstractTestCase {

    @Test(expected = BackgroundException.class)
    public void testCheckUnknown() throws Exception {
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        final FTPSession session = new FTPSession(new Host("unknownhost.local"));
        try {
            s.check(session, new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            });
        }
        catch(BackgroundException e) {
            assertEquals(UnknownHostException.class, e.getCause().getClass());
            assertEquals(Session.State.closed, session.getState());
            throw e;
        }
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        final DAVSession session = new DAVSession(new Host(Protocol.WEBDAV_SSL, "54.228.253.92", new Credentials("user", "p")));
        KeychainFactory.addFactory(Factory.NATIVE_PLATFORM, new KeychainFactory() {
            @Override
            protected AbstractKeychain create() {
                return new NullKeychain() {
                    @Override
                    public boolean isTrusted(final String hostname, final X509Certificate[] certs) {
                        return false;
                    }
                };
            }
        });
        final LoginController l = new AbstractLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                //
            }
        };
        LoginControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new LoginControllerFactory() {
            @Override
            protected LoginController create(final Controller c) {
                return l;
            }

            @Override
            protected LoginController create() {
                return l;
            }
        });
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        s.check(session, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        s.check(new FTPSession(new Host("")), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
    }
}
