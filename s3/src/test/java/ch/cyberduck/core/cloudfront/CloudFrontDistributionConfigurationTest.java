package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.amazonaws.services.cloudfront.model.StreamingDistribution;
import com.amazonaws.services.cloudfront.model.UpdateDistributionResult;
import com.amazonaws.services.cloudfront.model.UpdateStreamingDistributionResult;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CloudFrontDistributionConfigurationTest extends AbstractS3Test {

    @Test
    public void testGetMethods() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        assertEquals(Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING),
            new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                Collections.emptyMap()).getMethods(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetName() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(
            session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        assertEquals("Amazon CloudFront", configuration.getName());
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.CUSTOM));
    }

    @Test
    public void testGetOrigin() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        assertEquals("bbb.s3.amazonaws.com",
            configuration.getOrigin(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume)), Distribution.DOWNLOAD).getHost());
    }

    @Test
    public void testReadDownload() throws Exception {
        final DistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
        assertEquals("ETW0HTI5PZK7X", distribution.getId());
        assertEquals(Distribution.DOWNLOAD, distribution.getMethod());
        assertEquals("Deployed", distribution.getStatus());
        assertEquals("test-us-east-1-cyberduck.s3.amazonaws.com", distribution.getOrigin().getHost());
        assertEquals(URI.create("http://dc7v3c6g3gz6c.cloudfront.net"), distribution.getUrl());
        assertNull(distribution.getIndexDocument());
        assertNull(distribution.getErrorDocument());
    }

    @Test
    public void testReadStreaming() throws Exception {
        final DistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.STREAMING, new DisabledLoginCallback());
        assertEquals("E25267XDMTRRIW", distribution.getId());
        assertEquals("test-us-east-1-cyberduck.s3.amazonaws.com", distribution.getOrigin().getHost());
        assertEquals(URI.create("rtmp://s9xwj9xzlfydi.cloudfront.net/cfx/st"), distribution.getUrl());
        assertNull(distribution.getIndexDocument());
        assertNull(distribution.getErrorDocument());
    }

    @Test(expected = LoginCanceledException.class)
    public void testReadLoginFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        final S3Session session = new S3Session(host);
        final DistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
    }

    @Test
    public void testReadLoginFailureFix() throws Exception {
        assertTrue(session.isConnected());
        session.getHost().getCredentials().setPassword(null);
        assertNull(session.getHost().getCredentials().getPassword());
        final DistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AtomicBoolean set = new AtomicBoolean();
        configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                set.set(true);
                return new Credentials(username, System.getProperties().getProperty("s3.secret"));
            }
        });
        assertTrue(set.get());
        session.close();
    }

    @Test
    public void testWriteNewStreaming() throws Exception {
        final AtomicBoolean set = new AtomicBoolean();
        final CloudFrontDistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session,
            new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap()) {
            @Override
            protected UpdateStreamingDistributionResult updateStreamingDistribution(final Path container, final Distribution distribution) {
                fail();
                return null;
            }

            @Override
            protected StreamingDistribution createStreamingDistribution(final Path container, final Distribution distribution) {
                set.set(true);
                return new StreamingDistribution().withId("");
            }
        };
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = new Distribution(Distribution.STREAMING, true);
        configuration.write(container, distribution, new DisabledLoginCallback());
        assertTrue(set.get());
    }

    @Test
    public void testWriteNewDownload() throws Exception {
        final AtomicBoolean set = new AtomicBoolean();
        final CloudFrontDistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session,
            new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap()) {
            @Override
            protected UpdateDistributionResult updateDownloadDistribution(final Path container, final Distribution distribution) {
                fail();
                return null;
            }

            @Override
            protected com.amazonaws.services.cloudfront.model.Distribution createDownloadDistribution(final Path container, final Distribution distribution) {
                set.set(true);
                return new com.amazonaws.services.cloudfront.model.Distribution().withId("");
            }
        };
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = new Distribution(Distribution.DOWNLOAD, true);
        configuration.write(container, distribution, new DisabledLoginCallback());
        assertTrue(set.get());
    }

    @Test
    public void testProtocol() {
        assertEquals("cloudfront.amazonaws.com", new CloudFrontDistributionConfiguration(
            new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname())), new DisabledX509TrustManager(), new DefaultX509KeyManager(),
            Collections.emptyMap()).getHostname());
    }

    @Test
    public void testFeatures() {
        final CloudFrontDistributionConfiguration d = new CloudFrontDistributionConfiguration(
            new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname())), new DisabledX509TrustManager(), new DefaultX509KeyManager(),
            Collections.emptyMap());
        assertNotNull(d.getFeature(Purge.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Purge.class, Distribution.WEBSITE_CDN));
        assertNull(d.getFeature(Purge.class, Distribution.STREAMING));
        assertNull(d.getFeature(Purge.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(Index.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Index.class, Distribution.WEBSITE_CDN));
        assertNull(d.getFeature(Index.class, Distribution.STREAMING));
        assertNull(d.getFeature(Index.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(DistributionLogging.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(Cname.class, Distribution.DOWNLOAD));
        assertNotNull(d.getFeature(IdentityConfiguration.class, Distribution.DOWNLOAD));
    }

    @Test
    public void testInvalidateWithWildcards() throws Exception {
        final CloudFrontDistributionConfiguration configuration
            = new CloudFrontDistributionConfiguration(session, new DisabledX509TrustManager(), new DefaultX509KeyManager(), Collections.emptyMap());
        final Path container = new Path("/test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new Path("/test-us-east-1-cyberduck/directory", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
        assertEquals("ETW0HTI5PZK7X", distribution.getId());
        configuration.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(container), new DisabledLoginCallback());
        configuration.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(directory), new DisabledLoginCallback());
    }
}
