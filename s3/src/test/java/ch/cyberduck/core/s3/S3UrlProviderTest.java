package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3UrlProviderTest extends AbstractS3Test {

    @Test
    public void testToHttpURL() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol() {
            @Override
            public String getAuthorization() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS2.name();
            }
        }, new S3Protocol().getDefaultHostname())) {
            @Override
            public RequestEntityRestStorageService getClient() {
                try {
                    return this.connect(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
                }
                catch(BackgroundException e) {
                    fail();
                    throw new RuntimeException(e);
                }
            }
        };
        Path p = new Path("/bucket/f/key f", EnumSet.of(Path.Type.file));
        assertEquals(5, new S3UrlProvider(session, Collections.emptyMap()).toUrl(p).filter(DescriptiveUrl.Type.signed).size());
    }

    @Test
    public void testWebUrl() {
        {
            final DescriptiveUrlBag list = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/key",
                    EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.http);
            assertEquals(2, list.size());
            final Iterator<DescriptiveUrl> provider = list.iterator();
            assertEquals("https://test-eu-west-1-cyberduck.s3.amazonaws.com/key", provider.next().getUrl());
            assertEquals("http://test-eu-west-1-cyberduck.s3.amazonaws.com/key", provider.next().getUrl());
        }
        session.getHost().setWebURL("https://cdn.cyberduck.io/");
        {
            final DescriptiveUrlBag list = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/key",
                    EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.http);
            assertEquals(3, list.size());
            final Iterator<DescriptiveUrl> provider = list.iterator();
            assertEquals("https://test-eu-west-1-cyberduck.s3.amazonaws.com/key", provider.next().getUrl());
            assertEquals("http://test-eu-west-1-cyberduck.s3.amazonaws.com/key", provider.next().getUrl());
            assertEquals("https://cdn.cyberduck.io/test-eu-west-1-cyberduck/key", provider.next().getUrl());
        }
    }

    @Test
    public void testProviderUriWithKey() {
        final Iterator<DescriptiveUrl> provider = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/key",
                EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.provider).iterator();
        assertEquals("s3://test-eu-west-1-cyberduck/key", provider.next().getUrl());
    }

    @Test
    public void testProviderUriRoot() {
        final Iterator<DescriptiveUrl> provider = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck",
                EnumSet.of(Path.Type.directory))).filter(DescriptiveUrl.Type.provider).iterator();
        assertEquals("s3://test-eu-west-1-cyberduck/", provider.next().getUrl());
    }

    @Test
    public void testHttpUri() {
        final Iterator<DescriptiveUrl> http = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/key",
                EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.http).iterator();
        assertEquals("https://test-eu-west-1-cyberduck.s3.amazonaws.com/key", http.next().getUrl());
        assertEquals("http://test-eu-west-1-cyberduck.s3.amazonaws.com/key", http.next().getUrl());
    }

    @Test
    public void testHttpUriCustomPort() {
        session.getHost().setPort(8443);
        final Iterator<DescriptiveUrl> http = new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/key",
                EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.http).iterator();
        assertEquals("https://test-eu-west-1-cyberduck.s3.amazonaws.com:8443/key", http.next().getUrl());
        assertEquals("http://test-eu-west-1-cyberduck.s3.amazonaws.com/key", http.next().getUrl());
    }

    @Test
    public void testToSignedUrlAnonymous() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials("anonymous", null))) {
            @Override
            public RequestEntityRestStorageService getClient() {
                try {
                    return this.connect(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
                }
                catch(BackgroundException e) {
                    fail();
                    throw new RuntimeException(e);
                }
            }
        };
        assertEquals(DescriptiveUrl.EMPTY,
                new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/test f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrlThirdparty() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "s.greenqloud.com",
                new Credentials("k", "s"))) {
            @Override
            public RequestEntityRestStorageService getClient() {
                try {
                    return this.connect(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
                }
                catch(BackgroundException e) {
                    fail();
                    throw new RuntimeException(e);
                }
            }
        };
        final S3UrlProvider provider = new S3UrlProvider(session, Collections.emptyMap());
        assertNotNull(
                provider.toUrl(new Path("/test-eu-west-1-cyberduck/test", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrl() throws Exception {
        final S3UrlProvider provider = new S3UrlProvider(session, Collections.emptyMap());
        final Path bucket = new Path("/test-eu-west-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3ObjectListService(session, new S3AccessControlListFeature(session)).list(bucket);
        assertTrue(provider.toSignedUrl(new Path(bucket, "test", EnumSet.of(Path.Type.file)), 30).getUrl().startsWith(
                "https://test-eu-west-1-cyberduck.s3.dualstack.eu-west-1.amazonaws.com/test?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date="));
    }

    @Test
    public void testToSignedUrlVirtualHost() throws Exception {
        final S3UrlProvider provider = new S3UrlProvider(virtualhost, Collections.emptyMap());
        final Path bucket = Home.root();
        new S3ObjectListService(virtualhost, new S3AccessControlListFeature(virtualhost)).list(bucket);
        final String url = provider.toSignedUrl(new Path(bucket, "t", EnumSet.of(Path.Type.file)), 30).getUrl();
        assertTrue(url, url.startsWith(
                "https://test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com/t?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date="));
    }

    @Test
    public void testPlaceholder() {
        assertTrue(
                new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/test", EnumSet.of(Path.Type.directory))).filter(DescriptiveUrl.Type.signed).isEmpty());
    }
}
