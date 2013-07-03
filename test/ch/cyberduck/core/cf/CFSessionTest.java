package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.ProfileReaderFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CFSessionTest extends AbstractTestCase {

    @Test
    public void testConnectRackspace() throws Exception {
        final Host host = new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(), new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final CFSession session = new CFSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        assertFalse(session.cache().isEmpty());
        final CFPath container = new CFPath(session, "/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertNull(session.toHttpURL(new CFPath(session, container, "d/f", Path.FILE_TYPE)));
        container.attributes().setRegion("DFW");
        assertEquals("http://2b72124779a6075376a9-dc3ef5db7541ebd1f458742f9170bbe4.r64.cf1.rackcdn.com/d/f", session.toHttpURL(new CFPath(session, container, "d/f", Path.FILE_TYPE)));
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectRackspaceLon() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/Rackspace UK.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final CFSession session = new CFSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(), new Credentials(
                "a", "s"
        ));
        final CFSession session = new CFSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test
    public void testConnectHp() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final CFSession session = new CFSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testFile() {
        final CFSession session = new CFSession(new Host(Protocol.SWIFT, "h"));
        assertFalse(session.isCreateFileSupported(new CFPath(session, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new CFPath(session, "/container", Path.VOLUME_TYPE)));
    }
}
