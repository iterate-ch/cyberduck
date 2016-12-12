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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class AzureAttributesFinderFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        f.find(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotNull(attributes.getETag());
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
