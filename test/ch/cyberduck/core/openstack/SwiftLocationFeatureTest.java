package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SwiftLocationFeatureTest extends AbstractTestCase {

    @Test
    public void testGetLocations() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertTrue(new SwiftLocationFeature(session).getLocations().contains("DFW"));
        assertTrue(new SwiftLocationFeature(session).getLocations().contains("ORD"));
        assertTrue(new SwiftLocationFeature(session).getLocations().contains("SYD"));
    }
}
