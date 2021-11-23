package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.proxy.Proxy;
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
    public void testToHttpURL() {
        final S3Session session = new S3Session(new Host(new S3Protocol() {
            @Override
            public String getAuthorization() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS2.name();
            }
        }, new S3Protocol().getDefaultHostname())) {
            @Override
            public RequestEntityRestStorageService getClient() {
                return this.connect(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            }
        };
        Path p = new Path("/bucket/f/key f", EnumSet.of(Path.Type.file));
        assertEquals(5, new S3UrlProvider(session, Collections.emptyMap(), new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
                return "k";
            }
        }).toUrl(p).filter(DescriptiveUrl.Type.signed).size());
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
    public void testToSignedUrlAnonymous() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
            new Credentials("anonymous", null))) {
            @Override
            public RequestEntityRestStorageService getClient() {
                return this.connect(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            }
        };
        assertEquals(DescriptiveUrl.EMPTY,
            new S3UrlProvider(session, Collections.emptyMap(), new DisabledPasswordStore() {
                @Override
                public String findLoginPassword(final Host bookmark) {
                    return "k";
                }
            }).toUrl(new Path("/test-eu-west-1-cyberduck/test f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrlThirdparty() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "s.greenqloud.com",
            new Credentials("k", "s"))) {
            @Override
            public RequestEntityRestStorageService getClient() {
                return this.connect(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            }
        };
        final S3UrlProvider provider = new S3UrlProvider(session, Collections.emptyMap(), new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
                return "k";
            }
        });
        assertNotNull(
            provider.toUrl(new Path("/test-eu-west-1-cyberduck/test", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrl() {
        final S3UrlProvider provider = new S3UrlProvider(session, Collections.emptyMap(), new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
                return "k";
            }
        });
        assertTrue(provider.toSignedUrl(new Path("/test-eu-west-1-cyberduck/test", EnumSet.of(Path.Type.file)), 30).getUrl().startsWith(
            "https://test-eu-west-1-cyberduck.s3.amazonaws.com/test?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential="));
    }

    @Test
    public void testPlaceholder() {
        assertTrue(
            new S3UrlProvider(session, Collections.emptyMap()).toUrl(new Path("/test-eu-west-1-cyberduck/test", EnumSet.of(Path.Type.directory))).filter(DescriptiveUrl.Type.signed).isEmpty());
    }
}
