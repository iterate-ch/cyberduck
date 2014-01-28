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
 * @version $Id:$
 */
public class AzureMoveFeatureTest extends AbstractTestCase {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path("cyberduck", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new AzureTouchFeature(session).touch(test);
        assertTrue(new AzureFindFeature(session).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new AzureMoveFeature(session).move(test, target, false);
        assertFalse(new AzureFindFeature(session).find(test));
        assertTrue(new AzureFindFeature(session).find(target));
        new AzureDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginController());
    }

    @Test
    public void testSupport() throws Exception {
        assertFalse(new AzureMoveFeature(null).isSupported(new Path("/c", Path.DIRECTORY_TYPE)));
        assertTrue(new AzureMoveFeature(null).isSupported(new Path("/c/f", Path.DIRECTORY_TYPE)));
    }
}
