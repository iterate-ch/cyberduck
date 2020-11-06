package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftFindFeatureTest extends AbstractSwiftTest {

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        assertTrue(new SwiftFindFeature(session).find(container));
    }

    @Test
    public void testFindKey() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertFalse(new SwiftFindFeature(session).find(file));
        try {
            new DefaultAttributesFinderFeature(session).find(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(file, new TransferStatus());
        assertTrue(new SwiftFindFeature(session).find(file));
        assertNotNull(new DefaultAttributesFinderFeature(session).find(file));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SwiftFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
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
        assertTrue(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertFalse(new SwiftFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }
}
