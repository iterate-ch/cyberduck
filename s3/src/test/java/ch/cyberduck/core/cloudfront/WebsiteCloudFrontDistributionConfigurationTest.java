package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class WebsiteCloudFrontDistributionConfigurationTest {

    @Test
    public void testGetMethodsAWS() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        final WebsiteCloudFrontDistributionConfiguration configuration = new WebsiteCloudFrontDistributionConfiguration(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertTrue(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.DOWNLOAD));
        assertTrue(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.STREAMING));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.CUSTOM));
        assertTrue(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE_CDN));
        assertTrue(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb_b", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE));
    }

    @Test
    public void testGetMethodsNonAWS() throws Exception {
        final S3Session session = new S3Session(new Host(new TestProtocol()));
        final WebsiteCloudFrontDistributionConfiguration configuration = new WebsiteCloudFrontDistributionConfiguration(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.DOWNLOAD));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.STREAMING));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.CUSTOM));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE_CDN));
        assertTrue(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE));
        assertFalse(configuration.getMethods(
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/bbb_b", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(Distribution.WEBSITE));
    }

    @Test
    public void testGetName() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration = new WebsiteCloudFrontDistributionConfiguration(
                session, new DisabledX509TrustManager(), new DefaultX509KeyManager()
        );
        assertEquals("Amazon CloudFront", configuration.getName());
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.DOWNLOAD));
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.STREAMING));
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.WEBSITE_CDN));
        assertEquals("Website Configuration (HTTP)", configuration.getName(Distribution.WEBSITE));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration = new WebsiteCloudFrontDistributionConfiguration(
                session, new DisabledX509TrustManager(), new DefaultX509KeyManager()
        );
        assertEquals("bbb.s3.amazonaws.com",
                configuration.getOrigin(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume)), Distribution.DOWNLOAD).getHost());
        assertEquals("bbb.s3.amazonaws.com",
                configuration.getOrigin(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume)), Distribution.WEBSITE).getHost());
        final Path container = new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("US");
        assertEquals("bbb.s3-website-us-east-1.amazonaws.com",
                configuration.getOrigin(container, Distribution.WEBSITE_CDN).getHost());
        container.attributes().setRegion("us-west-2");
        assertEquals("bbb.s3-website-us-west-2.amazonaws.com",
                configuration.getOrigin(container, Distribution.WEBSITE_CDN).getHost());
    }

    @Test
    public void testReadNoWebsiteConfiguration() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final WebsiteCloudFrontDistributionConfiguration configuration
                = new WebsiteCloudFrontDistributionConfiguration(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.WEBSITE, new DisabledLoginCallback());
        assertEquals("The specified bucket does not have a website configuration", distribution.getStatus());
    }

    @Test
    public void testFeatures() {
        final CloudFrontDistributionConfiguration d = new WebsiteCloudFrontDistributionConfiguration(
                new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname())),
                new DisabledX509TrustManager(), new DefaultX509KeyManager()
        );
        assertNotNull(d.getFeature(Purge.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Purge.class, Distribution.WEBSITE_CDN));
        assertNull(d.getFeature(Purge.class, Distribution.STREAMING));
        assertNull(d.getFeature(Purge.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(Index.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Index.class, Distribution.WEBSITE_CDN));
        assertNotNull(d.getFeature(Index.class, Distribution.WEBSITE));
        assertNull(d.getFeature(Index.class, Distribution.STREAMING));
        assertNotNull(d.getFeature(DistributionLogging.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Cname.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Cname.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(IdentityConfiguration.class, Distribution.DOWNLOAD));
    }
}

