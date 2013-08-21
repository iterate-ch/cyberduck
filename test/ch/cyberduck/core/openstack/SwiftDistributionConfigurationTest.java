package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetName() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        assertEquals("Akamai", configuration.getName());
        assertEquals("Akamai", configuration.getName(Distribution.DOWNLOAD));
    }

    @Test
    public void testReadRackspace() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        container.attributes().setRegion("DFW");
        final Distribution test = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginController());
        assertNotNull(test);
        assertEquals(Distribution.DOWNLOAD, test.getMethod());
        assertEquals("http://2b72124779a6075376a9-dc3ef5db7541ebd1f458742f9170bbe4.r64.cf1.rackcdn.com/d/f",
                configuration.toUrl(new Path(container, "d/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.cdn).getUrl());
        assertArrayEquals(new String[]{}, test.getCNAMEs());
        assertEquals("index.html", test.getIndexDocument());
        assertNull(test.getErrorDocument());
        assertEquals("None", test.getInvalidationStatus());
        assertTrue(test.isEnabled());
        assertTrue(test.isDeployed());
        assertTrue(test.isLogging());
        assertEquals("test.cyberduck.ch", test.getId());
        assertEquals(1, test.getContainers().size());
        assertEquals(".CDN_ACCESS_LOGS", test.getLoggingTarget());
        assertEquals("storage101.dfw1.clouddrive.com", test.getOrigin());
    }

    @Test
    public void testReadHpcloud() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "region-a.geo-1.identity.hpcloudsvc.com", 35357);
        host.setCredentials(new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        final Path container = new Path(new Path(String.valueOf(Path.DELIMITER),
                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE), "test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("region-a.geo-1");
        final Distribution test = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginController());
        assertNotNull(test);
        assertEquals(Distribution.DOWNLOAD, test.getMethod());
        assertArrayEquals(new String[]{}, test.getCNAMEs());
        assertEquals("index.html", test.getIndexDocument());
        assertNull(test.getErrorDocument());
        assertEquals("None", test.getInvalidationStatus());
        assertTrue(test.isEnabled());
        assertTrue(test.isDeployed());
        assertFalse(test.isLogging());
        assertEquals("test.cyberduck.ch", test.getId());
        assertEquals("http://h2c0a3c89b6b2779528b78c25aeab0958.cdn.hpcloudsvc.com", test.getUrl());
        assertEquals("https://a248.e.akamai.net/cdn.hpcloudsvc.com/h2c0a3c89b6b2779528b78c25aeab0958/prodaw2", test.getSslUrl());
        assertEquals(1, test.getContainers().size());
        assertEquals("region-a.geo-1.objects.hpcloudsvc.com", test.getOrigin());
    }
}