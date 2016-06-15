package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Ignore
@Category(IntegrationTest.class)
public class CustomOriginCloudFrontDistributionConfigurationTest {

    @Test
    public void testGetMethods() throws Exception {
        assertEquals(Arrays.asList(Distribution.CUSTOM),
                new CustomOriginCloudFrontDistributionConfiguration(new Host(new TestProtocol()), new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener()).getMethods(
                        new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final Host h = new Host(new TestProtocol());
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        h.setWebURL("http://w.example.net");
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(h, new DefaultX509TrustManager(), new DefaultX509KeyManager(),
                new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        h.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        h.setWebURL("f");
        assertEquals("f", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
    }

    @Test
    public void testGetOriginCustomHttpPort() throws Exception {
        final Host h = new Host(new TestProtocol());
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        h.setWebURL("http://w.example.net:8080");
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(h, new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(8080, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        h.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test
    public void testGetOriginCustomHttpsPort() throws Exception {
        final Host h = new Host(new TestProtocol());
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        h.setWebURL("https://w.example.net:4444");
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(h, new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals("https", configuration.getOrigin(container, Distribution.CUSTOM).getScheme());
        assertEquals(4444, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        h.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test
    public void testReadNoConfiguredDistributionForOrigin() throws Exception {
        final Host origin = new Host(new TestProtocol(), "myhost.localdomain");
        origin.getCdnCredentials().setUsername(System.getProperties().getProperty("s3.key"));
        origin.getCdnCredentials().setPassword(System.getProperties().getProperty("s3.secret"));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
                //
            }
        }, new DefaultX509KeyManager(), new DisabledTranscriptListener());
        final Path container = new Path("unknown.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.CUSTOM, new DisabledLoginCallback());
        assertNull(distribution.getId());
        assertEquals("myhost.localdomain", distribution.getOrigin().getHost());
        assertEquals("Unknown", distribution.getStatus());
        assertEquals(null, distribution.getId());
    }

    @Test
    public void testRead() throws Exception {
        final Host origin = new Host(new TestProtocol(), "myhost.localdomain");
        origin.setWebURL("http://test-us-east-1-cyberduck");
        origin.setDefaultPath("public_html");
        origin.getCdnCredentials().setUsername(System.getProperties().getProperty("s3.key"));
        origin.getCdnCredentials().setPassword(System.getProperties().getProperty("s3.secret"));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
                //
            }
        }, new DefaultX509KeyManager(), new DisabledTranscriptListener());
        final Distribution distribution = configuration.read(new Path("/public_html", EnumSet.of(Path.Type.directory)), Distribution.CUSTOM, new DisabledLoginCallback());
        assertEquals("E230LC0UG2YLKV", distribution.getId());
        assertEquals("http://test-us-east-1-cyberduck/public_html", distribution.getOrigin().toString());
        assertEquals("http://test-us-east-1-cyberduck/f", configuration.toUrl(new Path("/public_html/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin).getUrl());
        assertEquals(Distribution.CUSTOM, distribution.getMethod());
        assertEquals("http://d1f6cbdjcbzyiu.cloudfront.net", distribution.getUrl().toString());
        assertEquals(null, distribution.getIndexDocument());
        assertEquals(null, distribution.getErrorDocument());
        assertEquals("log.test-us-east-1-cyberduck", distribution.getLoggingContainer());
    }

    @Test(expected = LoginCanceledException.class)
    public void testReadMissingCredentials() throws Exception {
        final Host bookmark = new Host(new TestProtocol(), "myhost.localdomain");
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(bookmark, new DefaultX509TrustManager(), new DefaultX509KeyManager(),
                new DisabledTranscriptListener());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        configuration.read(container, Distribution.CUSTOM, new DisabledLoginCallback());
    }
}
