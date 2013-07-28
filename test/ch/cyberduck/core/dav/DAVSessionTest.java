package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;

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
        assertNotNull(session.mount(new DisabledListProgressListener()));
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

    @Test(expected = LoginFailureException.class)
    public void testRedirect301() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-perm");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect302() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-tmp");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
    }

    @Test(expected = LoginFailureException.class)
    public void testRedirect303() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-other");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount(new DisabledListProgressListener()));
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testRedirectGone() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/redir-gone");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
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
        assertNotNull(session.mount(new DisabledListProgressListener()));
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
        session.mkdir(test, null);
        assertTrue(session.exists(test));
        session.delete(test, new DisabledLoginController());
        assertFalse(session.exists(test));
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
        session.getFeature(Touch.class, new DisabledLoginController()).touch(test);
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
        assertNotNull(session.list(session.home(), new DisabledListProgressListener()));
        session.close();
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
        session.open(new DefaultHostKeyController());
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
        });
    }

    @Test
    public void testFeatures() throws Exception {
        final Session session = new DAVSession(new Host("h"));
        assertNull(session.getFeature(UnixPermission.class, null));
        assertNull(session.getFeature(Timestamp.class, null));
        assertNotNull(session.getFeature(Copy.class, null));
        assertNotNull(session.getFeature(Headers.class, null));
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
    }
}
