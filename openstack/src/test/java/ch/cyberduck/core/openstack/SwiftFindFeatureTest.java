package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

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
    public void testFindKeyWithSamePrefix() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String prefix = new AlphanumericRandomStringService().random();
        final Path other = new Path(container, String.format("%s.%s", prefix, new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(other, new TransferStatus());
        final Path file = new Path(container, prefix, EnumSet.of(Path.Type.file));
        final SwiftFindFeature feature = new SwiftFindFeature(session);
        assertFalse(feature.find(file));
        assertFalse(feature.find(new Path(file).withType(EnumSet.of(Path.Type.directory))));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(file, new TransferStatus());
        assertTrue(feature.find(file));
        assertFalse(feature.find(new Path(file).withType(EnumSet.of(Path.Type.directory))));
        assertFalse(feature.find(new Path(String.format("%s-", file.getAbsolute()), EnumSet.of(Path.Type.file))));
        assertFalse(feature.find(new Path(String.format("%s-", file.getAbsolute()), EnumSet.of(Path.Type.directory))));
        assertFalse(feature.find(new Path(String.format("-%s", file.getAbsolute()), EnumSet.of(Path.Type.file))));
        assertFalse(feature.find(new Path(String.format("-%s", file.getAbsolute()), EnumSet.of(Path.Type.directory))));
        assertNotNull(new SwiftAttributesFinderFeature(session).find(file));
        new SwiftDeleteFeature(session).delete(Arrays.asList(file, other), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindKeyWithSameSuffix() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String suffix = new AlphanumericRandomStringService().random();
        final Path other = new Path(container, String.format("%s.%s", new AlphanumericRandomStringService().random(), suffix), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(other, new TransferStatus());
        final Path file = new Path(container, suffix, EnumSet.of(Path.Type.file));
        final SwiftFindFeature feature = new SwiftFindFeature(session);
        assertFalse(feature.find(file));
        assertFalse(feature.find(new Path(file).withType(EnumSet.of(Path.Type.directory))));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(file, new TransferStatus());
        assertTrue(feature.find(file));
        assertFalse(feature.find(new Path(file).withType(EnumSet.of(Path.Type.directory))));
        assertFalse(feature.find(new Path(String.format("%s-", file.getAbsolute()), EnumSet.of(Path.Type.file))));
        assertFalse(feature.find(new Path(String.format("%s-", file.getAbsolute()), EnumSet.of(Path.Type.directory))));
        assertFalse(feature.find(new Path(String.format("-%s", file.getAbsolute()), EnumSet.of(Path.Type.file))));
        assertFalse(feature.find(new Path(String.format("-%s", file.getAbsolute()), EnumSet.of(Path.Type.directory))));
        assertNotNull(new SwiftAttributesFinderFeature(session).find(file));
        new SwiftDeleteFeature(session).delete(Arrays.asList(file, other), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
        assertTrue(new SwiftObjectListService(session).list(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener()).contains(test));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertFalse(new SwiftFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(cache, new SwiftFindFeature(session)).find(directory));
        assertFalse(cache.isCached(directory));
    }
}
