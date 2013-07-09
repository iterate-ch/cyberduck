package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.ui.Controller;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

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
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.cache().containsKey(new Path("/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new Path("/trunk", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new Path("/branches", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new Path("/tags", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = BackgroundException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController());
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
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        session.close();
    }

    @Test
    @Ignore
    public void testBasicWebdavOrg() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                "user1", "user1"
        ));
        host.setDefaultPath("/auth-basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        session.close();
    }

    @Test
    @Ignore
    public void testDigestWebdavOrg() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                "user1", "user1"
        ));
        host.setDefaultPath("/auth-digest");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        session.close();
    }

    @Test
    @Ignore
    public void testRedirect301() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        session.close();
    }

    @Test
    @Ignore
    public void testRedirect302() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
    }

    @Test
    @Ignore
    public void testRedirect303() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    @Ignore
    public void testRedirectGone() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.webdav.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNull(session.mount());
        session.close();
    }

    @Test
    public void testLoginBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        session.close();
    }

    @Test
    public void testMakeDirectory() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test);
        assertTrue(session.exists(test));
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.touch(test);
        assertTrue(session.exists(test));
        session.delete(test, new DisabledLoginController());
        assertFalse(session.exists(test));
        session.close();
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        assertNotNull(session.list(session.home()));
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        session.close();
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
                return new DisabledLoginController() {
                    @Override
                    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                                       final LoginOptions options) throws LoginCanceledException {
                        assertEquals(host.getCredentials(), credentials);
                        assertEquals("Login failed", title);
                        assertEquals("Authorization Required.", reason);
                        assertFalse(options.publickey);
                        throw new LoginCanceledException();
                    }
                };
            }
        });
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
    }

    @Test
    public void testHttpUrl() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final DAVSession session = new DAVSession(host);
        assertEquals("http://test.cyberduck.ch/my/documentroot/f", session.toURL(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)));
        assertEquals("http://test.cyberduck.ch/my/documentroot/f", session.toHttpURL(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)));
    }


    @Test
    public void testFeatures() throws Exception {
        final Session session = new DAVSession(new Host("h"));
        assertNull(session.getFeature(UnixPermission.class, null));
        assertNull(session.getFeature(Timestamp.class, null));
        assertNotNull(session.getFeature(Headers.class, null));
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
    }
}
