package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
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

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.BlobType;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureWriteFeatureTest extends AbstractAzureTest {

    @Test
    public void testWriteOverrideAppendBlob() throws Exception {
        final OperationContext context
            = new OperationContext();
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = RandomUtils.nextBytes(513);
        status.setLength(content.length);
        status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length)));
        status.setMetadata(Collections.singletonMap("Cache-Control", "public,max-age=86400"));
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session, BlobType.APPEND_BLOB, context).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new AzureFindFeature(session, context).find(test));
        final PathAttributes attributes = new AzureAttributesFinderFeature(session, context).find(test);
        assertEquals(content.length, attributes.getSize());
        final Map<String, String> metadata = new AzureMetadataFeature(session, context).getMetadata(test);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("public,max-age=86400", metadata.get("Cache-Control"));
        assertEquals(content.length, new AzureWriteFeature(session, context).append(test, status.withRemote(attributes)).size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session, context).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final OutputStream overwrite = new AzureWriteFeature(session, context).write(test, new TransferStatus().exists(true)
            .withLength("overwrite".getBytes(StandardCharsets.UTF_8).length).withMetadata(Collections.singletonMap("Content-Type", "text/plain")), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus())
            .transfer(new ByteArrayInputStream("overwrite".getBytes(StandardCharsets.UTF_8)), overwrite);
        overwrite.close();
        // Test double close
        overwrite.close();
        assertEquals("overwrite".getBytes(StandardCharsets.UTF_8).length, new AzureAttributesFinderFeature(session, context).find(test).getSize());
        new AzureDeleteFeature(session, context).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteOverrideBlockBlob() throws Exception {
        final OperationContext context
            = new OperationContext();
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = RandomUtils.nextBytes(513);
        status.setLength(content.length);
        status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length)));
        status.setMetadata(Collections.singletonMap("Cache-Control", "public,max-age=86400"));
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session, BlobType.BLOCK_BLOB, context).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new AzureFindFeature(session, context).find(test));
        final PathAttributes attributes = new AzureAttributesFinderFeature(session, context).find(test);
        assertEquals(content.length, attributes.getSize());
        final Map<String, String> metadata = new AzureMetadataFeature(session, context).getMetadata(test);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("public,max-age=86400", metadata.get("Cache-Control"));
        final Write.Append append = new AzureWriteFeature(session, context).append(test, status.withRemote(attributes));
        assertFalse(append.append);
        assertEquals(0L, append.size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session, context).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final OutputStream overwrite = new AzureWriteFeature(session, context).write(test, new TransferStatus().exists(true)
            .withLength("overwrite".getBytes(StandardCharsets.UTF_8).length).withMetadata(Collections.singletonMap("Content-Type", "text/plain")), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus())
            .transfer(new ByteArrayInputStream("overwrite".getBytes(StandardCharsets.UTF_8)), overwrite);
        overwrite.close();
        // Test double close
        overwrite.close();
        assertEquals("overwrite".getBytes(StandardCharsets.UTF_8).length, new AzureAttributesFinderFeature(session, context).find(test).getSize());
        new AzureDeleteFeature(session, context).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
