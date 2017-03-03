package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
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
public class CloudFrontDistributionConfigurationTest {

    @Test
    public void testGetMethods() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        assertEquals(Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING),
                new CloudFrontDistributionConfiguration(session
                ).getMethods(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetName() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(
                session);
        assertEquals("Amazon CloudFront", configuration.getName());
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.CUSTOM));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        assertEquals("bbb.s3.amazonaws.com",
                configuration.getOrigin(new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume)), Distribution.DOWNLOAD).getHost());
    }

    @Test
    public void testReadDownload() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
        assertEquals("ETW0HTI5PZK7X", distribution.getId());
        assertEquals(Distribution.DOWNLOAD, distribution.getMethod());
        assertEquals("Deployed", distribution.getStatus());
        assertEquals("test-us-east-1-cyberduck.s3.amazonaws.com", distribution.getOrigin().getHost());
        assertEquals(URI.create("http://dc7v3c6g3gz6c.cloudfront.net"), distribution.getUrl());
        assertNull(distribution.getIndexDocument());
        assertEquals(null, distribution.getErrorDocument());
    }

    @Test
    public void testReadStreaming() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.STREAMING, new DisabledLoginCallback());
        assertEquals("E25267XDMTRRIW", distribution.getId());
        assertEquals("test-us-east-1-cyberduck.s3.amazonaws.com", distribution.getOrigin().getHost());
        assertEquals(URI.create("rtmp://s9xwj9xzlfydi.cloudfront.net/cfx/st"), distribution.getUrl());
        assertEquals(null, distribution.getIndexDocument());
        assertEquals(null, distribution.getErrorDocument());
    }

    @Test(expected = LoginCanceledException.class)
    public void testReadLoginFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        final S3Session session = new S3Session(host);
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
    }

    @Test
    public void testReadLoginFailureFix() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), null);
        final S3Session session = new S3Session(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword(System.getProperties().getProperty("s3.secret"));
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(session.isConnected());
        host.getCredentials().setPassword(null);
        assertNull(host.getCredentials().getPassword());
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AtomicBoolean set = new AtomicBoolean();
        configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword(System.getProperties().getProperty("s3.secret"));
                set.set(true);
            }
        });
        assertTrue(set.get());
        session.close();
    }

    @Test
    public void testWriteNewStreaming() throws Exception {
        final AtomicBoolean set = new AtomicBoolean();
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final CloudFrontDistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session) {
            @Override
            protected UpdateStreamingDistributionResult updateStreamingDistribution(final Path container, final Distribution distribution) throws IOException, ConnectionCanceledException {
                fail();
                return null;
            }

            @Override
            protected StreamingDistribution createStreamingDistribution(final Path container, final Distribution distribution) throws ConnectionCanceledException {
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
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final CloudFrontDistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session) {
            @Override
            protected UpdateDistributionResult updateDownloadDistribution(final Path container, final Distribution distribution) throws IOException, ConnectionCanceledException {
                fail();
                return null;
            }

            @Override
            protected com.amazonaws.services.cloudfront.model.Distribution createDownloadDistribution(final Path container, final Distribution distribution) throws ConnectionCanceledException {
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
                new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()))
        ).getHostname());
    }

    @Test
    public void testFeatures() {
        final CloudFrontDistributionConfiguration d = new CloudFrontDistributionConfiguration(
                new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()))
        );
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
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setCredentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final CloudFrontDistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session);
        final Path container = new Path("/test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new Path("/test-us-east-1-cyberduck/directory", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD, new DisabledLoginCallback());
        assertEquals("ETW0HTI5PZK7X", distribution.getId());
        configuration.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(container), new DisabledLoginCallback());
        configuration.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(directory), new DisabledLoginCallback());
    }
}
