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
        ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        try {
            s.check(new FTPSession(new Host("unknownhost.local")));
        }
        catch(BackgroundException e) {
            assertEquals(UnknownHostException.class, e.getCause().getClass());
            throw e;
        }
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testHandshakeFailure() throws Exception {
        KeychainFactory.addFactory(Factory.NATIVE_PLATFORM, new KeychainFactory() {
            @Override
            protected AbstractKeychain create() {
                return new NoTrustKeychain();
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
        ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        s.check(new DAVSession(new Host(Protocol.WEBDAV_SSL, "54.228.253.92", new Credentials("user", "p"))));
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        ConnectionCheckService s = new ConnectionCheckService(new DisabledLoginController(), new DefaultHostKeyController());
        s.check(new FTPSession(new Host("")));
    }

    private final class NoTrustKeychain extends NullKeychain {
        @Override
        public boolean isTrusted(final String hostname, final X509Certificate[] certs) {
            return false;
        }
    }
}
