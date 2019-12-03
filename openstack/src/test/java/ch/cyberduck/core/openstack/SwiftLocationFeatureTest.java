package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Path;
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
public class SwiftLocationFeatureTest extends AbstractSwiftTest {

    @Test
    public void testGetLocations() throws Exception {
        final Set<Location.Name> locations = new SwiftLocationFeature(session).getLocations();
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("DFW")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("ORD")));
        assertTrue(locations.contains(new SwiftLocationFeature.SwiftRegion("SYD")));
        assertEquals(new SwiftLocationFeature.SwiftRegion("DFW"), locations.iterator().next());
    }

    @Test
    public void testCache() throws Exception {
        final SwiftLocationFeature feature = new SwiftLocationFeature(session);
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), feature.getLocation(
            new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
// Cache
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), feature.getLocation(
            new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testLocationNull() {
        final SwiftLocationFeature.SwiftRegion region = new SwiftLocationFeature.SwiftRegion(null);
        assertNull(region.getIdentifier());
        assertEquals("Unknown", region.toString());
    }

    @Test(expected = NotfoundException.class)
    public void testLookupContainerNotfound() throws Exception {
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SwiftLocationFeature(session).getLocation(container);
    }

    @Test
    public void testFindLocation() throws Exception {
        assertEquals(new SwiftLocationFeature.SwiftRegion("IAD"), new SwiftLocationFeature(session).getLocation(
            new Path("cdn.duck.sh", EnumSet.of(Path.Type.volume, Path.Type.directory))));
        assertEquals(unknown, new SwiftLocationFeature(session).getLocation(
            new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testEquals() {
        assertEquals(unknown, new SwiftLocationFeature.SwiftRegion(null));
    }
}
