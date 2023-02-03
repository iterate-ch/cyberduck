package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftTouchFeatureTest extends AbstractSwiftTest {

    @Test
    public void testSupported() {
        assertFalse(new SwiftTouchFeature(session, new SwiftRegionService(session)).isSupported(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), StringUtils.EMPTY));
        assertTrue(new SwiftTouchFeature(session, new SwiftRegionService(session)).isSupported(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume)), StringUtils.EMPTY));
    }

    @Test
    public void testTouch() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes attributes = new SwiftAttributesFinderFeature(session).find(test);
        assertEquals(test.attributes().getChecksum(), attributes.getChecksum());
        assertNotEquals(-1L, attributes.getModificationDate());
        final Path found = new SwiftObjectListService(session, new SwiftRegionService(session)).list(container, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertTrue(found.isFile());
        assertEquals(attributes, found.attributes());
    }
}
