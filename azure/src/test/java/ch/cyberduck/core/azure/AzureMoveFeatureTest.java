package ch.cyberduck.core.azure;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
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
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AzureMoveFeatureTest {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new AzureMoveFeature(session, null).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session, null).find(test));
        assertTrue(new AzureFindFeature(session, null).find(target));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSupport() throws Exception {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new AzureMoveFeature(null, null).isSupported(c, c));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new AzureMoveFeature(null, null).isSupported(cf, cf));
    }
}
