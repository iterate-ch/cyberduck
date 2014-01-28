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
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class AzureMetadataFeatureTest extends AbstractTestCase {


    @Test
    public void testSetMetadata() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DefaultHostKeyController(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.empty());
        final Path container = new Path("cyberduck", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", Path.FILE_TYPE);
        new AzureTouchFeature(session).touch(test);
        final String v = UUID.randomUUID().toString();
        new AzureMetadataFeature(session).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new AzureMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
        session.close();
    }
}
