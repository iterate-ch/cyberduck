package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Location;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static ch.cyberduck.core.features.Location.unknown;
import static org.junit.Assert.*;

public class SwiftLocationFeatureTest extends AbstractSwiftTest {

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
    public void testEquals() {
        assertEquals(unknown, new SwiftLocationFeature.SwiftRegion(null));
    }
}
