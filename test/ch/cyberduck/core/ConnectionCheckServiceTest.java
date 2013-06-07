package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.ui.Controller;

import org.junit.Test;

import java.net.UnknownHostException;

/**
 * @version $Id:$
 */
public class ConnectionCheckServiceTest extends AbstractTestCase {

    @Test(expected = UnknownHostException.class)
    public void testCheckUnknown() throws Exception {
        ConnectionCheckService s = new ConnectionCheckService();
        s.check(new FTPSession(new Host("unknownhost.local")));
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        final LoginController l = new AbstractLoginController() {

            @Override
            public void warn(final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                //
            }

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
            protected LoginController create(final Session s) {
                return l;
            }

            @Override
            protected LoginController create() {
                return l;
            }
        });
        ConnectionCheckService s = new ConnectionCheckService();
        s.check(new DAVSession(new Host(Protocol.WEBDAV_SSL, "54.228.253.92", new Credentials("user", "p"))));
    }
}
