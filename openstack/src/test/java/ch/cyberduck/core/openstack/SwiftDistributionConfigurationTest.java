package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftDistributionConfigurationTest {

    @Test
    public void testGetName() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        assertEquals("Akamai", configuration.getName());
        assertEquals("Akamai", configuration.getName(Distribution.DOWNLOAD));
    }

    @Test
    public void testFeatures() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        assertNotNull(configuration.getFeature(Purge.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(Index.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(DistributionLogging.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(IdentityConfiguration.class, Distribution.DOWNLOAD));
        assertNotNull(configuration.getFeature(AnalyticsProvider.class, Distribution.DOWNLOAD));
        assertNull(configuration.getFeature(Cname.class, Distribution.DOWNLOAD));
    }

    @Test
    public void testReadRackspace() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        final Distribution test = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
        assertNotNull(test);
        assertEquals(Distribution.DOWNLOAD, test.getMethod());
        assertEquals("http://2b72124779a6075376a9-dc3ef5db7541ebd1f458742f9170bbe4.r64.cf1.rackcdn.com/d/f",
                configuration.toUrl(new Path(container, "d/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.cdn).getUrl());
        assertArrayEquals(new String[]{}, test.getCNAMEs());
        assertEquals("index.html", test.getIndexDocument());
        assertNull(test.getErrorDocument());
        assertEquals("None", test.getInvalidationStatus());
        assertTrue(test.isEnabled());
        assertTrue(test.isDeployed());
        assertTrue(test.isLogging());
        assertEquals("test.cyberduck.ch", test.getId());
        assertEquals(1, test.getContainers().size());
        assertEquals(".CDN_ACCESS_LOGS", test.getLoggingContainer());
        assertEquals("storage101.dfw1.clouddrive.com", test.getOrigin().getHost());
        assertEquals(URI.create("https://storage101.dfw1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test.cyberduck.ch"),
                test.getOrigin());
        session.close();
    }

    @Test
    public void testWriteRackspace() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration = new SwiftDistributionConfiguration(session);
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("ORD");
        new SwiftDirectoryFeature(session).mkdir(container, "ORD", new TransferStatus());
        configuration.write(container, new Distribution(Distribution.DOWNLOAD, true), new DisabledLoginCallback());
        assertTrue(configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback()).isEnabled());
        new SwiftDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}