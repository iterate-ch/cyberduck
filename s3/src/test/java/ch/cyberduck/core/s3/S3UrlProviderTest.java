package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.util.EnumSet;
import java.util.Iterator;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3UrlProviderTest {

    @Test
    public void testToHttpURL() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        Path p = new Path("/bucket/f/key f", EnumSet.of(Path.Type.file));
        assertEquals(5, new S3UrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                return "k";
            }
        }).toUrl(p).filter(DescriptiveUrl.Type.signed).size());
    }

    @Test
    public void testProviderUriWithKey() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final Iterator<DescriptiveUrl> provider = new S3UrlProvider(session).toUrl(new Path("/test.cyberduck.ch/key",
                EnumSet.of(Path.Type.file))).filter(DescriptiveUrl.Type.provider).iterator();
        assertEquals("https://s3.amazonaws.com/test.cyberduck.ch/key", provider.next().getUrl());
        assertEquals("s3://test.cyberduck.ch/key", provider.next().getUrl());
    }

    @Test
    public void testProviderUriRoot() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final Iterator<DescriptiveUrl> provider = new S3UrlProvider(session).toUrl(new Path("/test.cyberduck.ch",
                EnumSet.of(Path.Type.directory))).filter(DescriptiveUrl.Type.provider).iterator();
        assertEquals("https://s3.amazonaws.com/test.cyberduck.ch", provider.next().getUrl());
        assertEquals("s3://test.cyberduck.ch/", provider.next().getUrl());
    }

    @Test
    public void testHttpUri() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/key",
                new S3UrlProvider(session).toUrl(new Path("/test.cyberduck.ch/key", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testToSignedUrlAnonymous() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials("anonymous", null)));
        assertEquals(DescriptiveUrl.EMPTY,
                new S3UrlProvider(session, new DisabledPasswordStore() {
                    @Override
                    public String find(final Host host) {
                        return "k";
                    }
                }).toUrl(new Path("/test.cyberduck.ch/test f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrlThirdparty() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "s.greenqloud.com",
                new Credentials("k", "s")));
        final S3UrlProvider provider = new S3UrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                return "k";
            }
        });
        assertNotNull(
                provider.toUrl(new Path("/test.cyberduck.ch/test", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed)
        );
    }

    @Test
    public void testToSignedUrl() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), null
        )));
        final S3UrlProvider provider = new S3UrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                return "k";
            }
        });
        assertTrue(provider.sign(new Path("/test.cyberduck.ch/test", EnumSet.of(Path.Type.file)), 30).getUrl().startsWith(
                "https://test.cyberduck.ch.s3.amazonaws.com/test?AWSAccessKeyId=AKIAIGNLFZ2PXC6H2UPQ&Expires="));
    }

    @Test
    public void testToTorrentUrl() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials("anonymous", null)));
        assertEquals(new DescriptiveUrl(URI.create("http://test.cyberduck.ch.s3.amazonaws.com/test%20f?torrent"), DescriptiveUrl.Type.torrent),
                new S3UrlProvider(session).toUrl(new Path("/test.cyberduck.ch/test f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.torrent));
    }

    @Test
    public void testToTorrentUrlThirdparty() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "test.cyberduck.ch",
                new Credentials("anonymous", null)));
        assertEquals(new DescriptiveUrl(URI.create("http://test.cyberduck.ch/c/test%20f?torrent"), DescriptiveUrl.Type.torrent),
                new S3UrlProvider(session).toUrl(new Path("/c/test f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.torrent));
    }

    @Test
    public void testPlaceholder() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), null
        )));
        assertTrue(
                new S3UrlProvider(session).toUrl(new Path("/test.cyberduck.ch/test", EnumSet.of(Path.Type.directory))).filter(DescriptiveUrl.Type.signed).isEmpty());
    }
}