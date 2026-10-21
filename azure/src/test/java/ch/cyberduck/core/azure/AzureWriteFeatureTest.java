package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobType;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureWriteFeatureTest extends AbstractAzureTest {

    @Test
    public void testWriteOverrideAppendBlob() throws Exception {
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = RandomUtils.nextBytes(513);
        status.setLength(content.length);
        status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus().setLength(content.length)));
        status.setMetadata(Collections.singletonMap("Cache-Control", "public,max-age=86400"));
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session, BlobType.APPEND_BLOB).write(test, status, ConnectionCallback.noop);
        assertNotNull(out);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new AzureFindFeature(session).find(test));
        final PathAttributes attributes = new AzureAttributesFinderFeature(session).find(test);
        assertEquals(content.length, attributes.getSize());
        final Map<String, String> metadata = new AzureMetadataFeature(session).getMetadata(test);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("public,max-age=86400", metadata.get("Cache-Control"));
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session).read(test, new TransferStatus(), ConnectionCallback.noop);
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final OutputStream overwrite = new AzureWriteFeature(session).write(test, new TransferStatus().setExists(true)
                .setLength("overwrite".getBytes(StandardCharsets.UTF_8).length).setMetadata(Collections.singletonMap("Content-Type", "text/plain")), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop)
                .transfer(new ByteArrayInputStream("overwrite".getBytes(StandardCharsets.UTF_8)), overwrite);
        overwrite.close();
        // Test double close
        overwrite.close();
        assertEquals("overwrite".getBytes(StandardCharsets.UTF_8).length, new AzureAttributesFinderFeature(session).find(test).getSize());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteOverrideBlockBlob() throws Exception {
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = RandomUtils.nextBytes(513);
        status.setLength(content.length);
        status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus().setLength(content.length)));
        status.setMetadata(Collections.singletonMap("Cache-Control", "public,max-age=86400"));
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session, BlobType.BLOCK_BLOB).write(test, status, ConnectionCallback.noop);
        assertNotNull(out);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new AzureFindFeature(session).find(test));
        final PathAttributes attributes = new AzureAttributesFinderFeature(session).find(test);
        assertEquals(content.length, attributes.getSize());
        final Map<String, String> metadata = new AzureMetadataFeature(session).getMetadata(test);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("public,max-age=86400", metadata.get("Cache-Control"));
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session).read(test, new TransferStatus(), ConnectionCallback.noop);
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final OutputStream overwrite = new AzureWriteFeature(session).write(test, new TransferStatus().setExists(true)
                .setLength("overwrite".getBytes(StandardCharsets.UTF_8).length).setMetadata(Collections.singletonMap("Content-Type", "text/plain")), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop)
                .transfer(new ByteArrayInputStream("overwrite".getBytes(StandardCharsets.UTF_8)), overwrite);
        overwrite.close();
        // Test double close
        overwrite.close();
        assertEquals("overwrite".getBytes(StandardCharsets.UTF_8).length, new AzureAttributesFinderFeature(session).find(test).getSize());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteOverwriteAppendBlobRetainsAppendBlobType() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] initial = RandomUtils.nextBytes(513);
        final OutputStream out = new AzureWriteFeature(session, BlobType.APPEND_BLOB).write(test, new TransferStatus().setLength(initial.length), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(initial), out);
        out.close();
        assertEquals(BlobType.APPEND_BLOB.name(), new AzureAttributesFinderFeature(session).find(test).getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE));
        // Type of existing blob is retained
        final byte[] content = RandomUtils.nextBytes(1025);
        final OutputStream overwrite = new AzureWriteFeature(session, BlobType.BLOCK_BLOB).write(test, new TransferStatus().setExists(true).setLength(content.length), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(content), overwrite);
        overwrite.close();
        final PathAttributes attributes = new AzureAttributesFinderFeature(session).find(test);
        assertEquals(BlobType.APPEND_BLOB.name(), attributes.getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE));
        assertEquals(content.length, attributes.getSize());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session).read(test, new TransferStatus(), ConnectionCallback.noop);
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteOverwriteBlockBlobRetainsBlockBlobType() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] initial = RandomUtils.nextBytes(513);
        final OutputStream out = new AzureWriteFeature(session, BlobType.BLOCK_BLOB).write(test, new TransferStatus().setLength(initial.length), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(initial), out);
        out.close();
        assertEquals(BlobType.BLOCK_BLOB.name(), new AzureAttributesFinderFeature(session).find(test).getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE));
        final byte[] content = RandomUtils.nextBytes(1025);
        final OutputStream overwrite = new AzureWriteFeature(session, BlobType.APPEND_BLOB).write(test, new TransferStatus().setExists(true).setLength(content.length), ConnectionCallback.noop);
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).transfer(new ByteArrayInputStream(content), overwrite);
        overwrite.close();
        final PathAttributes attributes = new AzureAttributesFinderFeature(session).find(test);
        assertEquals(BlobType.BLOCK_BLOB.name(), attributes.getCustom().get(AzureAttributesFinderFeature.KEY_BLOB_TYPE));
        assertEquals(content.length, attributes.getSize());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteAppendBlobBuffersCommittedBlocks() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2 * 1024 * 1024);
        final OutputStream out = new AzureWriteFeature(session, BlobType.APPEND_BLOB).write(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        // Write in chunks smaller than the block size
        for(int offset = 0; offset < content.length; offset += 32768) {
            out.write(content, offset, 32768);
        }
        out.close();
        final BlobClient client = session.getClient().getBlobServiceClient().getBlobContainerClient("cyberduck").getBlobClient(test.getName());
        assertEquals(content.length, new AzureAttributesFinderFeature(session).find(test).getSize());
        // Each write of the SDK stream would otherwise be committed as a separate block, limiting the blob to 50,000 blocks
        assertEquals(1, client.getProperties().getCommittedBlockCount().intValue());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
