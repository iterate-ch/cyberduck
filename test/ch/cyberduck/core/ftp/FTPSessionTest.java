package ch.cyberduck.core.ftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.threading.BackgroundException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FTPSessionTest extends AbstractTestCase {

    @Test
    public void testConnectAnonymous() throws Exception {
        final Host host = new Host(Protocol.FTP, "mirror.switch.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        assertEquals(Session.State.closed, session.getState());
        assertNotNull(session.open());
        assertEquals(Session.State.open, session.getState());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertEquals(Session.State.closed, session.getState());
        assertNull(session.getClient());
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledLoginController());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test
    public void testConnectionTlsUpgrade() throws Exception {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host) {
            @Override
            protected void warn(final LoginController login) throws BackgroundException {
                assertEquals(Session.State.open, this.getState());
                super.warn(login);
                assertEquals(Protocol.FTP_TLS, host.getProtocol());
            }

            protected boolean isTLSSupported() throws BackgroundException {
                final boolean s = super.isTLSSupported();
                assertTrue(s);
                return true;
            }
        };
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertEquals(Protocol.FTP, host.getProtocol());
        LoginService l = new LoginService(new DisabledLoginController());
        l.login(session, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
        assertEquals(Protocol.FTP_TLS, host.getProtocol());
    }
}
