package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.Set;

import static ch.cyberduck.core.features.Location.unknown;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLocationFeatureTest {

    @Test
    public void testGetLocations() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Set<Location.Name> locations = new SwiftLocationFeature(session).getLocations();
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("DFW")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("ORD")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("SYD")));
        assertEquals(new SwiftLocationFeature.SwiftRegion("DFW"), locations.iterator().next());
        session.close();
    }

    @Test
    public void testCache() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SwiftLocationFeature feature = new SwiftLocationFeature(session);
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), feature.getLocation(
                new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
        session.close();
        // Cache
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), feature.getLocation(
                new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testLocationNull() throws Exception {
        final SwiftLocationFeature.SwiftRegion region = new SwiftLocationFeature.SwiftRegion(null);
        assertNull(region.getIdentifier());
        assertEquals("Unknown", region.toString());
    }

    @Test(expected = NotfoundException.class)
    public void testLookupContainerNotfound() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SwiftLocationFeature(session).getLocation(container);
    }

    @Test
    public void testFindLocation() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), new SwiftLocationFeature(session).getLocation(
                new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
        assertEquals(unknown, new SwiftLocationFeature(session).getLocation(
                new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))));
        session.close();
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(unknown, new SwiftLocationFeature.SwiftRegion(null));
    }
}
