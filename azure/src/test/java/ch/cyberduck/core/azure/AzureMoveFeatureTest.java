package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AzureMoveFeatureTest extends AbstractAzureTest {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureTouchFeature(session, null).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(test));
        final Path target = new AzureMoveFeature(session, null).move(test, new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new AzureFindFeature(session, null).find(test));
        assertTrue(new AzureFindFeature(session, null).find(target));
        final PathAttributes targetAttr = new AzureAttributesFinderFeature(session, null).find(target);
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new AzureMoveFeature(session, null).isSupported(c, Optional.of(new Path("/d", EnumSet.of(Path.Type.directory)))));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new AzureMoveFeature(session, null).isSupported(cf, Optional.of(new Path("/c/f2", EnumSet.of(Path.Type.directory)))));
    }
}
