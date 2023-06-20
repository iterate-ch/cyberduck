package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureDirectoryFeatureTest extends AbstractAzureTest {

    @Test
    public void testCreateContainer() throws Exception {
        final AzureDirectoryFeature feature = new AzureDirectoryFeature(session, null);
        final Path container = feature.mkdir(new Path(new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(container));
        assertEquals(container.attributes(), new AzureAttributesFinderFeature(session, null).find(container));
        assertThrows(ConflictException.class, () -> feature.mkdir(container, new TransferStatus()));
        new AzureTouchFeature(session, null).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(container.attributes(), new AzureAttributesFinderFeature(session, null).find(container));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session, null).find(container));
    }

    @Test(expected = InteroperabilityException.class)
    public void testCreateContainerInvalidName() throws Exception {
        final Path container = new Path("untitled folder", EnumSet.of(Path.Type.directory));
        final AzureDirectoryFeature feature = new AzureDirectoryFeature(session, null);
        assertFalse(feature.isSupported(container.getParent(), container.getName()));
        feature.mkdir(container, new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(container));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session, null).find(container));
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Path container = new Path("/cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path placeholder = new AzureDirectoryFeature(session, null).mkdir(new Path(container, new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(placeholder.getType().contains(Path.Type.placeholder));
        assertTrue(new AzureFindFeature(session, null).find(placeholder));
        assertEquals(placeholder.attributes(), new AzureAttributesFinderFeature(session, null).find(placeholder));
        new AzureTouchFeature(session, null).touch(new Path(placeholder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(placeholder.attributes(), new AzureAttributesFinderFeature(session, null).find(placeholder));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session, null).find(placeholder));
    }
}
