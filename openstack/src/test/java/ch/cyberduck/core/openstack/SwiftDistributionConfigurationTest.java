package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
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
public class SwiftDistributionConfigurationTest extends AbstractSwiftTest {

    @Test
    public void testGetName() throws Exception {
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        assertEquals("Akamai", configuration.getName());
    }

    @Test
    public void testFeatures() throws Exception {
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        assertNotNull(configuration.getFeature(Purge.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(Index.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(DistributionLogging.class, Distribution.DOWNLOAD));
        assertNull(configuration.getFeature(Cname.class, Distribution.DOWNLOAD));
    }

    @Test
    public void testWriteDownloadConfigurationRackspace() throws Exception {
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session
        );
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(container, new TransferStatus().withRegion("ORD"));
        configuration.write(container, new Distribution(Distribution.DOWNLOAD, true), new DisabledLoginCallback());
        assertTrue(configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback()).isEnabled());
        new SwiftDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteWebsiteConfigurationRackspace() throws Exception {
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session
        );
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(container, new TransferStatus().withRegion("ORD"));
        final Distribution config = new Distribution(Distribution.WEBSITE, true);
        config.setIndexDocument("index.html");
        configuration.write(container, config, new DisabledLoginCallback());
        final Distribution distribution = configuration.read(container, Distribution.WEBSITE, new DisabledLoginCallback());
        assertTrue(distribution.isEnabled());
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(container);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("X-Container-Meta-Web-Index"));
        assertEquals("index.html", metadata.get("X-Container-Meta-Web-Index"));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
