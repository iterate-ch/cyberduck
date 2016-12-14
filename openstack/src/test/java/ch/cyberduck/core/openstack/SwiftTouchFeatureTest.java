package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftTouchFeatureTest {

    @Test
    public void testFile() {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        assertFalse(new SwiftTouchFeature(new SwiftWriteFeature(session, new SwiftRegionService(session))).isSupported(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new SwiftTouchFeature(new SwiftWriteFeature(session, new SwiftRegionService(session))).isSupported(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }
}
