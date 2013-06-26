package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
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
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
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
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
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
            session.login(new DisabledLoginController());
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
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
    }

    @Test
    public void testFile() {
        final CFSession session = new CFSession(new Host(Protocol.SWIFT, "h"));
        assertFalse(session.isCreateFileSupported(new CFPath(session, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new CFPath(session, "/container", Path.VOLUME_TYPE)));
    }
}
