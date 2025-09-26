package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.InvalidFilenameException;
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
        final AzureDirectoryFeature feature = new AzureDirectoryFeature(session);
        final Path container = feature.mkdir(new AzureWriteFeature(session), new Path(new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(container));
        assertEquals(PathAttributes.EMPTY, container.attributes());
        assertThrows(ConflictException.class, () -> feature.mkdir(new AzureWriteFeature(session), container, new TransferStatus()));
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session).find(container));
    }

    @Test(expected = InteroperabilityException.class)
    public void testCreateContainerInvalidName() throws Exception {
        final Path container = new Path("untitled folder", EnumSet.of(Path.Type.directory));
        final AzureDirectoryFeature feature = new AzureDirectoryFeature(session);
        assertFalse(feature.isSupported(container.getParent(), container.getName()));
        assertThrows(InvalidFilenameException.class, () -> feature.preflight(container.getParent(), container.getName()));
        feature.mkdir(new AzureWriteFeature(session), container, new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(container));
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session).find(container));
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Path container = new Path("/cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path placeholder = new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(placeholder.getType().contains(Path.Type.placeholder));
        assertTrue(new AzureFindFeature(session).find(placeholder));
        assertEquals(PathAttributes.EMPTY, placeholder.attributes());
        final Path file = new Path(placeholder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), file, new TransferStatus());
        new AzureDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        // Still find common prefix
        assertTrue(new AzureFindFeature(session).find(placeholder));
        assertEquals(PathAttributes.EMPTY, new AzureAttributesFinderFeature(session).find(placeholder));
        new AzureDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
