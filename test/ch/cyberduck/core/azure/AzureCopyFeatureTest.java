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

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class AzureCopyFeatureTest extends AbstractTestCase {

    @Test
    public void testCopy() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path("cyberduck", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new AzureTouchFeature(session).touch(test);
        final Path copy = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new AzureCopyFeature(session).copy(test, copy);
        assertTrue(new AzureFindFeature(session).find(test));
        assertTrue(new AzureFindFeature(session).find(copy));
        new AzureDeleteFeature(session).delete(Arrays.asList(test, copy), new DisabledLoginController());
        session.close();
    }
}
