package ch.cyberduck.core.gstorage;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class GSWebsiteDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetOrigin() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GSSession(new Host(Protocol.GOOGLESTORAGE_SSL, Protocol.GOOGLESTORAGE_SSL.getDefaultHostname())));
        assertEquals(Arrays.asList(Distribution.WEBSITE), configuration.getMethods(null));
    }

    @Test
    public void testGetProtocol() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GSSession(new Host(Protocol.GOOGLESTORAGE_SSL, Protocol.GOOGLESTORAGE_SSL.getDefaultHostname())));
        assertEquals(Protocol.GOOGLESTORAGE_SSL, configuration.getProtocol());
    }
}
