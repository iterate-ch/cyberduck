package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CustomOriginCloudFrontDistributionConfigurationTest {

    @Test
    public void testGetMethods() throws Exception {
        assertEquals(Collections.singletonList(Distribution.CUSTOM),
                new CustomOriginCloudFrontDistributionConfiguration(new Host(new TestProtocol()), new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener()).getMethods(
                        new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("http://w.example.net");
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager(),
                new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        origin.setWebURL("f");
        assertEquals("f", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
    }

    @Test
    public void testGetOriginCustomHttpPort() throws Exception {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("http://w.example.net:8080");
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(8080, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test
    public void testGetOriginCustomHttpsPort() throws Exception {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("https://w.example.net:4444");
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals("https", configuration.getOrigin(container, Distribution.CUSTOM).getScheme());
        assertEquals(4444, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test(expected = NotfoundException.class)
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
    @Ignore
    public void testWriteReadUpdate() throws Exception {
        final Host origin = new Host(new TestProtocol(), String.format("%s.localdomain", UUID.randomUUID().toString()));
        origin.setWebURL("http://example.net");
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
        final Path file = new Path("/public_html", EnumSet.of(Path.Type.directory));
        final Distribution writeDistributionConfiguration = new Distribution(Distribution.CUSTOM, false);
        // Create
        configuration.write(file, writeDistributionConfiguration, new DisabledLoginCallback());
        // Read
        final Distribution readDistributionConfiguration = configuration.read(file, Distribution.CUSTOM, new DisabledLoginCallback());
        assertNotNull(readDistributionConfiguration.getId());
        assertFalse(readDistributionConfiguration.isEnabled());
        assertEquals("http://example.net/public_html", readDistributionConfiguration.getOrigin().toString());
        assertEquals("http://example.net/f", configuration.toUrl(new Path("/public_html/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin).getUrl());
        assertEquals(Distribution.CUSTOM, readDistributionConfiguration.getMethod());
        assertNull(readDistributionConfiguration.getIndexDocument());
        assertNull(readDistributionConfiguration.getErrorDocument());
        assertNull(readDistributionConfiguration.getLoggingContainer());
        readDistributionConfiguration.setEnabled(false);
        // Update
        configuration.write(file, readDistributionConfiguration, new DisabledLoginCallback());
        configuration.deleteDownloadDistribution(file, readDistributionConfiguration);
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
