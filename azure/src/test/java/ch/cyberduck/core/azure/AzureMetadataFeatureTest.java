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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureMetadataFeatureTest {

    @Test
    public void testSetMetadata() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        final String v = UUID.randomUUID().toString();
        new AzureMetadataFeature(session, null).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new AzureMetadataFeature(session, null).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSetCacheControl() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        final AzureMetadataFeature service = new AzureMetadataFeature(session, null);
        service.setMetadata(test, Collections.<String, String>singletonMap("Cache-Control",
                "public, max-age=0"));
        final Map<String, String> metadata = service.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Cache-Control"));
        assertEquals("public, max-age=0", metadata.get("Cache-Control"));
        // Make sure content type is not deleted
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("application/octet-stream", metadata.get("Content-Type"));
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
