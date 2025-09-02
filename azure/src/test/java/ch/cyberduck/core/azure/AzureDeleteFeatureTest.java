package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AzureDeleteFeatureTest extends AbstractAzureTest {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(test));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session).find(test));
    }

    @Test
    public void testDeleteKey() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new AzureDirectoryFeature(session).mkdir(container, new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(test));
        new AzureDeleteFeature(session).delete(Arrays.asList(container, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session).find(test));
    }
}
