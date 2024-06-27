package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureCopyFeatureTest extends AbstractAzureTest {

    @Test
    public void testCopy() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureTouchFeature(session, null).touch(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        Thread.sleep(1000L);
        final AzureCopyFeature feature = new AzureCopyFeature(session, null);
        assertThrows(UnsupportedException.class, () -> feature.preflight(container, test));
        try {
            feature.preflight(container, test);
        }
        catch(UnsupportedException e) {
            assertEquals("Unsupported", e.getMessage());
            assertEquals("Cannot copy cyberduck.", e.getDetail(false));
        }
        final Path copy = feature.copy(test,
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(test.attributes().getChecksum(), copy.attributes().getChecksum());
        assertNotEquals(new AzureAttributesFinderFeature(session, null).find(test).getModificationDate(), new AzureAttributesFinderFeature(session, null).find(copy).getModificationDate());
        assertTrue(new AzureFindFeature(session, null).find(test));
        assertTrue(new AzureFindFeature(session, null).find(copy));
        new AzureDeleteFeature(session, null).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new AzureDirectoryFeature(session, null).mkdir(folder, new TransferStatus());
        final Path test = new AzureTouchFeature(session, null).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = new AzureWriteFeature(session, null).write(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final Path copy = new AzureTouchFeature(session, null).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new AzureCopyFeature(session, null).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(1023L, new AzureAttributesFinderFeature(session, null).find(copy).getSize());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new AzureDeleteFeature(session, null).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
