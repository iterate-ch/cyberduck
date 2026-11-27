package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureMetadataFeatureTest extends AbstractAzureTest {

    @Test
    public void testSetMetadata() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), test, new TransferStatus());
        final String v = new AlphanumericRandomStringService().random();
        new AzureMetadataFeature(session).setMetadata(test, Collections.singletonMap("Test", v));
        final Map<String, String> metadata = new AzureMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetCacheControl() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), test, new TransferStatus());
        final AzureMetadataFeature service = new AzureMetadataFeature(session);
        service.setMetadata(test, Collections.singletonMap("Cache-Control", "public, max-age=0"));
        final Map<String, String> metadata = service.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Cache-Control"));
        assertEquals("public, max-age=0", metadata.get("Cache-Control"));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFoundWithAttributesFinder() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String t = new AlphanumericRandomStringService().random();
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), test, new TransferStatus().setMetadata(Collections.singletonMap("Touch", t)));
        final Map<String, String> update = new HashMap<>(new AzureMetadataFeature(session).getMetadata(test));
        assertEquals(t, update.get("Touch"));
        final String v = new AlphanumericRandomStringService().random();
        update.put("Test", v);
        new AzureMetadataFeature(session).setMetadata(test, update);
        {
            final PathAttributes attributes = new AzureBlobAttributesFinderFeature(session).find(test);
            final Map<String, String> metadata = attributes.getMetadata();
            assertTrue(metadata.containsKey("Test"));
            assertEquals(v, metadata.get("Test"));
            assertEquals(t, metadata.get("Touch"));
            assertEquals(new AzureMetadataFeature(session).getMetadata(test), metadata);
        }
        {
            final AttributedList<Path> list = new AzureObjectListService(session).list(container, new DisabledListProgressListener());
            final Path found = list.find(new SimplePathPredicate(test));
            assertNotNull(found);
            final Map<String, String> metadata = found.attributes().getMetadata();
            assertTrue(metadata.containsKey("Test"));
            assertEquals(v, metadata.get("Test"));
            assertEquals(t, metadata.get("Touch"));
            assertEquals(new AzureMetadataFeature(session).getMetadata(test), metadata);
        }
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataTooLarge() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), test, new TransferStatus());
        final AzureMetadataFeature feature = new AzureMetadataFeature(session);
        final Map<String, String> metadata = Collections.singletonMap("k", StringUtils.repeat('a', 10 * 1024));
        // Same limit as when writing metadata with upload
        assertThrows(InteroperabilityException.class, () -> feature.setMetadata(test, metadata));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        final Path test = new AzureTouchFeature(session).touch(new AzureWriteFeature(session),
                new Path(new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new AzureListService(session).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new AzureListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new AzureMetadataFeature(session).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new AzureListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new AzureDeleteFeature(session).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
