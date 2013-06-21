package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.Controller;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVSessionTest extends AbstractTestCase {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
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

    @Test(expected = BackgroundException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open();
        try {
            session.login(new DisabledLoginController());
        }
        catch(BackgroundException e) {
            assertEquals("Connection failed", e.getMessage());
            assertEquals("Method Not Allowed.", e.getDetail());
            throw e;
        }
    }

    @Test
    @Ignore
    public void test404ErrorHeadWebdavOrg() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
    }

    @Test
    @Ignore
    public void testBasicWebdavOrg() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                "user1", "user1"
        ));
        host.setDefaultPath("/auth-basic");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
    }

    @Test
    @Ignore
    public void testDigestWebdavOrg() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                "user1", "user1"
        ));
        host.setDefaultPath("/auth-digest");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
    }

    @Test
    @Ignore
    public void testRedirect301() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
    }

    @Test
    @Ignore
    public void testRedirect302() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
    }

    @Test
    @Ignore
    public void testRedirect303() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
    }

    @Test(expected = BackgroundException.class)
    @Ignore
    public void testRedirectGone() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.login(new DisabledLoginController());
        assertNull(session.mount());
    }

    @Test
    public void testLoginBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.open();
        assertNotNull(session.home().list());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open();
        session.login(new DisabledLoginController());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureDigestAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/digest");
        final DAVSession session = new DAVSession(host);
        LoginControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new LoginControllerFactory() {
            @Override
            protected LoginController create(final Controller c) {
                return create();
            }

            @Override
            protected LoginController create() {
                return new AbstractLoginController() {
                    @Override
                    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                                       final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                        assertEquals(host.getCredentials(), credentials);
                        assertEquals("Login failed", title);
                        assertEquals("Authorization Required.", reason);
                        assertFalse(enablePublicKey);
                        throw new LoginCanceledException();
                    }
                };
            }
        });
        session.open();
        session.login(new DisabledLoginController());
    }
}
