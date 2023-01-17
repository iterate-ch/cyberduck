package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.test.VaultTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CustomOriginCloudFrontDistributionConfigurationTest extends VaultTest {

    @Test
    public void testGetMethods() {
        assertEquals(Collections.singletonList(Distribution.CUSTOM),
                new CustomOriginCloudFrontDistributionConfiguration(new Host(new TestProtocol()), new DefaultX509TrustManager(), new DefaultX509KeyManager()).getMethods(
                        new Path("/bbb", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetOrigin() {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("http://w.example.net");
        final CustomOriginCloudFrontDistributionConfiguration configuration
            = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        origin.setWebURL("f");
        assertEquals("f", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
    }

    @Test
    public void testGetOriginCustomHttpPort() {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("http://w.example.net:8080");
        final CustomOriginCloudFrontDistributionConfiguration configuration
            = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(8080, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test
    public void testGetOriginCustomHttpsPort() {
        final Host origin = new Host(new TestProtocol(), "m");
        final Path container = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        origin.setWebURL("https://w.example.net:4444");
        final CustomOriginCloudFrontDistributionConfiguration configuration
            = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals("https", configuration.getOrigin(container, Distribution.CUSTOM).getScheme());
        assertEquals(4444, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
        origin.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM).getHost());
        assertEquals(-1, configuration.getOrigin(container, Distribution.CUSTOM).getPort());
    }

    @Test
    public void testReadNoConfiguredDistributionForOrigin() throws Exception {
        final Host origin = new Host(new TestProtocol(), "myhost.localdomain");
        origin.getCdnCredentials().setUsername(PROPERTIES.get("s3.key"));
        origin.getCdnCredentials().setPassword(PROPERTIES.get("s3.secret"));
        final CustomOriginCloudFrontDistributionConfiguration configuration
                = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) {
                //
            }
        }, new DefaultX509KeyManager());
        final Path container = new Path("unknown.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Distribution distribution = configuration.read(container, Distribution.CUSTOM, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                return new Credentials(PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret"));
            }
        });
        assertFalse(distribution.isEnabled());
        assertEquals("Amazon CloudFront", distribution.getName());
    }

    @Test
    @Ignore
    public void testWriteReadUpdate() throws Exception {
        final Host origin = new Host(new TestProtocol(), String.format("%s.localdomain", UUID.randomUUID().toString()));
        origin.setWebURL("http://example.net");
        origin.setDefaultPath("public_html");
        final CustomOriginCloudFrontDistributionConfiguration configuration
            = new CustomOriginCloudFrontDistributionConfiguration(origin, new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) {
                //
            }
        }, new DefaultX509KeyManager());
        final Path file = new Path("/public_html", EnumSet.of(Path.Type.directory));
        final Distribution writeDistributionConfiguration = new Distribution(Distribution.CUSTOM, false);
        // Create
        final DisabledLoginCallback login = new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                return new Credentials(PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret"));
            }
        };
        configuration.write(file, writeDistributionConfiguration, login);
        // Read
        final Distribution distribution = configuration.read(file, Distribution.CUSTOM, login);
        assertNotNull(distribution.getId());
        assertFalse(distribution.isEnabled());
        assertEquals("http://example.net/public_html", distribution.getOrigin().toString());
        assertEquals("http://example.net/f", new DistributionUrlProvider(distribution)
            .toUrl(new Path("/public_html/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin).getUrl());
        assertEquals(Distribution.CUSTOM, distribution.getMethod());
        assertNull(distribution.getIndexDocument());
        assertNull(distribution.getErrorDocument());
        assertNull(distribution.getLoggingContainer());
        assertEquals("Amazon CloudFront", distribution.getName());
        distribution.setEnabled(false);
        // Update
        configuration.write(file, distribution, login);
        configuration.deleteDownloadDistribution(file, distribution);
    }

    @Test(expected = LoginCanceledException.class)
    public void testReadMissingCredentials() throws Exception {
        final Host bookmark = new Host(new TestProtocol(), "myhost.localdomain");
        final CustomOriginCloudFrontDistributionConfiguration configuration
            = new CustomOriginCloudFrontDistributionConfiguration(bookmark, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        configuration.read(container, Distribution.CUSTOM, new DisabledLoginCallback());
    }
}
