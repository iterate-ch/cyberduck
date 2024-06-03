package ch.cyberduck.core.azure;

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
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureAttributesFinderFeatureTest extends AbstractAzureTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session);
        f.find(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFind() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID() + ".txt", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotNull(attributes.getETag());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new AzureDirectoryFeature(session).mkdir(container, new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(container);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertNotNull(attributes.getETag());
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMissingPlaceholder() throws Exception {
        final Path container = new AzureDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String prefix = new AlphanumericRandomStringService().random();
        final Path intermediate = new Path(container, prefix, EnumSet.of(Path.Type.directory));
        final Path directory = new AzureDirectoryFeature(session).mkdir(new Path(intermediate, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(directory));
        assertEquals(directory.attributes(), new AzureAttributesFinderFeature(session).find(directory));
        final Path test = new AzureTouchFeature(session).touch(
                new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(container);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertNotNull(attributes.getETag());
        assertNotNull(new AzureObjectListService(session).list(directory, new DisabledListProgressListener()).find(new DefaultPathPredicate(test)));
        new AzureDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertNotNull(new AzureObjectListService(session).list(directory, new DisabledListProgressListener()).find(new DefaultPathPredicate(test)));
        // Still found as prefix
        assertNotNull(new AzureObjectListService(session).list(container, new DisabledListProgressListener()).find(new DefaultPathPredicate(intermediate)));
        assertNotNull(new AzureObjectListService(session).list(intermediate, new DisabledListProgressListener()).find(new DefaultPathPredicate(directory)));
        // Ignore 404 failures
        assertSame(PathAttributes.EMPTY, new AzureAttributesFinderFeature(session).find(directory));
        assertSame(PathAttributes.EMPTY, new AzureAttributesFinderFeature(session).find(intermediate));
        new AzureDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
