package ch.cyberduck.core.azure;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.storage.OperationContext;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureWriteFeatureTest {

    @Test
    public void testWriteOverride() throws Exception {
        final OperationContext context
                = new OperationContext();
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        status.setMetadata(Collections.singletonMap("Cache-Control", "public,max-age=86400"));
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session, context).write(test, status);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new AzureFindFeature(session, context).find(test));
        final PathAttributes attributes = new AzureAttributesFeature(session, context).find(test);
        assertEquals(content.length, attributes.getSize());
        final Map<String, String> metadata = new AzureMetadataFeature(session, context).getMetadata(test);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("public,max-age=86400", metadata.get("Cache-Control"));
        assertEquals(0L, new AzureWriteFeature(session, context).append(test, status.getLength(), PathCache.empty()).size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session, context).read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final OutputStream overwrite = new AzureWriteFeature(session, context).write(test, new TransferStatus()
                .length("overwrite".getBytes("UTF-8").length).metadata(Collections.singletonMap("Content-Type", "text/plain")));
        new StreamCopier(new TransferStatus(), new TransferStatus())
                .transfer(new ByteArrayInputStream("overwrite".getBytes("UTF-8")), overwrite);
        overwrite.close();
        // Test double close
        overwrite.close();
        assertEquals("overwrite".getBytes("UTF-8").length, new AzureAttributesFeature(session, context).find(test).getSize());
        new AzureDeleteFeature(session, context).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
