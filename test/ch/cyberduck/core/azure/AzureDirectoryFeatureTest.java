package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
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

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class AzureDirectoryFeatureTest extends AbstractTestCase {

    @Test
    public void testCreateContainer() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new AzureDirectoryFeature(session, null).mkdir(container, null);
        assertTrue(new AzureFindFeature(session, null).find(container));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new DisabledProgressListener());
        assertFalse(new AzureFindFeature(session, null).find(container));
    }


    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path("/cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path placeholder = new Path(container, UUID.randomUUID().toString(),
                EnumSet.of(Path.Type.directory));
        new AzureDirectoryFeature(session, null).mkdir(placeholder, null);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(new AzureFindFeature(session, null).find(placeholder));
        new AzureDeleteFeature(session, null).delete(Collections.<Path>singletonList(placeholder), new DisabledLoginCallback(), new DisabledProgressListener());
        assertFalse(new AzureFindFeature(session, null).find(placeholder));
    }
}
