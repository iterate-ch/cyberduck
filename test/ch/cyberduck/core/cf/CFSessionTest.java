package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

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
