package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureMetadataFeatureTest extends AbstractAzureTest {

    @Test
    public void testSetMetadata() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        final String v = new AlphanumericRandomStringService().random();
        new AzureMetadataFeature(session).setMetadata(test, Collections.singletonMap("Test", v));
        final Map<String, String> metadata = new AzureMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetCacheControl() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        final AzureMetadataFeature service = new AzureMetadataFeature(session);
        service.setMetadata(test, Collections.singletonMap("Cache-Control", "public, max-age=0"));
        final Map<String, String> metadata = service.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Cache-Control"));
        assertEquals("public, max-age=0", metadata.get("Cache-Control"));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
