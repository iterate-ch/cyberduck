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
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * @version $Id:$
 */
public class AzureDeleteFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), Path.VOLUME_TYPE);
        new AzureDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path("cyberduck", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
    }
}
