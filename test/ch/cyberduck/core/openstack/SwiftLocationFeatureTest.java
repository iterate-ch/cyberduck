package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.features.Location;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftLocationFeatureTest extends AbstractTestCase {

    @Test
    public void testGetLocations() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Set<Location.Name> locations = new SwiftLocationFeature(session).getLocations();
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("DFW")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("ORD")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("SYD")));
        assertEquals(new SwiftLocationFeature.SwiftRegion("DFW"), locations.iterator().next());
        session.close();
    }
}
