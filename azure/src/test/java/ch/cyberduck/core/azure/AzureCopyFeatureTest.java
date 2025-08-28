package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureCopyFeatureTest extends AbstractAzureTest {

    @Test
    public void testCopy() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureTouchFeature(session).touch(
                new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AzureCopyFeature feature = new AzureCopyFeature(session);
        assertThrows(UnsupportedException.class, () -> feature.preflight(container, Optional.of(test)));
        try {
            feature.preflight(container, Optional.of(test));
        }
        catch(UnsupportedException e) {
            assertEquals("Unsupported", e.getMessage());
            assertEquals("Cannot copy cyberduck.", e.getDetail(false));
        }
        final Path copy = feature.copy(test,
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(PathAttributes.EMPTY, copy.attributes());
        final PathAttributes sourceAttr = new AzureAttributesFinderFeature(session).find(test);
        final PathAttributes copyAttr = new AzureAttributesFinderFeature(session).find(copy);
        assertNotEquals(sourceAttr.getETag(), copyAttr.getETag());
        new AzureDeleteFeature(session).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), folder, new TransferStatus());
        final Path test = new AzureTouchFeature(session).touch(
                new AzureWriteFeature(session), new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = new AzureWriteFeature(session).write(test, new TransferStatus().setExists(true).setLength(content.length), new DisabledConnectionCallback());
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(content), out);
        final Path copy = new AzureTouchFeature(session).touch(
                new AzureWriteFeature(session), new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new AzureCopyFeature(session).copy(test, copy, new TransferStatus().setExists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(1023L, new AzureAttributesFinderFeature(session).find(copy).getSize());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new AzureDeleteFeature(session).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
