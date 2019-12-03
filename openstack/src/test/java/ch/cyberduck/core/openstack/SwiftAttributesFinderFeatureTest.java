package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftAttributesFinderFeatureTest extends AbstractSwiftTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final SwiftAttributesFinderFeature f = new SwiftAttributesFinderFeature(session);
        f.find(test);
    }

    @Test
    public void testFind() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = UUID.randomUUID().toString() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(test, new TransferStatus());
        final SwiftAttributesFinderFeature f = new SwiftAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals(EnumSet.of(Path.Type.file), test.getType());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertNotEquals(-1L, attributes.getModificationDate());
        // Test wrong type
        try {
            f.find(new Path(container, name, EnumSet.of(Path.Type.directory, Path.Type.placeholder)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final PathAttributes attributes = new SwiftAttributesFinderFeature(session).find(container);
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), container.getType());
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = UUID.randomUUID().toString();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(file, null, new TransferStatus());
        final PathAttributes attributes = new SwiftAttributesFinderFeature(session).find(file);
        // Test wrong type
        try {
            new SwiftAttributesFinderFeature(session).find(new Path(container, name, EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
