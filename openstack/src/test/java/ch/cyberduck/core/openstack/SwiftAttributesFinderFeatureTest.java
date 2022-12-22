package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
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

    @Test
    public void testFindRoot() throws Exception {
        final SwiftAttributesFinderFeature f = new SwiftAttributesFinderFeature(session);
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final SwiftAttributesFinderFeature f = new SwiftAttributesFinderFeature(session);
        try {
            f.find(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            f.find(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }

    @Test
    public void testFind() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
                new Path(container, String.format("%s-", name), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SwiftAttributesFinderFeature f = new SwiftAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals(EnumSet.of(Path.Type.file), test.getType());
        assertNull(attributes.getETag());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertEquals(attributes.getModificationDate(), new SwiftObjectListService(session).list(container, new DisabledListProgressListener())
                .find(new DefaultPathPredicate(test)).attributes().getModificationDate());
        assertNotEquals(-1L, attributes.getModificationDate());
        // Test wrong type
        try {
            f.find(new Path(container, name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            f.find(new Path(container, String.format("%s-", name), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final PathAttributes attributes = new SwiftAttributesFinderFeature(session).find(container);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertNotEquals(-1L, attributes.getSize());
        assertNotNull(attributes.getRegion());
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = UUID.randomUUID().toString();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(file, new TransferStatus());
        final PathAttributes attributes = new SwiftAttributesFinderFeature(session).find(file);
        assertNotNull(attributes.getChecksum().hash);
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

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        assertTrue(new SwiftFindFeature(session).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
                new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new SwiftAttributesFinderFeature(session).find(test));
        assertNotNull(new SwiftAttributesFinderFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new SwiftAttributesFinderFeature(session).find(test);
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            new SwiftAttributesFinderFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }
}
