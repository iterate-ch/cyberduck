package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Collections;
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
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        new AzureDirectoryFeature(session).mkdir(container, null);
        assertTrue(new AzureFindFeature(session).find(container));
        new AzureDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginController());
        assertFalse(new AzureFindFeature(session).find(container));
    }


    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path("/cyberduck", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        placeholder.attributes().setPlaceholder(true);
        new AzureDirectoryFeature(session).mkdir(placeholder, null);
        assertTrue(new AzureFindFeature(session).find(placeholder));
        new AzureDeleteFeature(session).delete(Collections.<Path>singletonList(placeholder), new DisabledLoginController());
        assertFalse(new AzureFindFeature(session).find(placeholder));
    }
}
