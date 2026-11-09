package ch.cyberduck.core.openstack;

import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;

import org.junit.Test;

import static org.junit.Assert.*;

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
}
