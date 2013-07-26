package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class S3UrlProviderTest extends AbstractTestCase {

    @Test
    public void testToURL() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        Path p = new Path("/bucket/f/key", Path.FILE_TYPE);
        assertEquals("https://bucket.s3.amazonaws.com/f/key",
                new S3UrlProvider(session).get(p).find(DescriptiveUrl.Type.provider).getUrl());
    }

    @Test
    public void testToHttpURL() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        Path p = new Path("/bucket/f/key", Path.FILE_TYPE);
        assertEquals("http://bucket.s3.amazonaws.com/f/key",
                new S3UrlProvider(session).get(p).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testUri() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/key",
                new S3UrlProvider(session).get(new Path("/test.cyberduck.ch/key", Path.FILE_TYPE)).find(DescriptiveUrl.Type.provider).getUrl());
    }

    @Test
    public void testHttpUri() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("http://test.cyberduck.ch.s3.amazonaws.com/key",
                new S3UrlProvider(session).get(new Path("/test.cyberduck.ch/key", Path.FILE_TYPE)).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testToSignedUrlAnonymous() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                new Credentials("anonymous", null)));
        assertEquals(DescriptiveUrl.EMPTY,
                new S3UrlProvider(session).get(new Path("/test.cyberduck.ch/test", Path.FILE_TYPE)).find(DescriptiveUrl.Type.signed));
    }

    @Test
    public void testToSignedUrlNoKeyFound() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), null
        )));
        assertEquals(DescriptiveUrl.EMPTY,
                new S3UrlProvider(session).createSignedUrl(new Path("/test.cyberduck.ch/test", Path.FILE_TYPE), 30));
    }

    @Test
    public void testToSignedUrl() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), null
        )));
        assertTrue(new S3UrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                return "k";
            }
        }).createSignedUrl(new Path("/test.cyberduck.ch/test", Path.FILE_TYPE), 30).getUrl().startsWith(
                "https://test.cyberduck.ch.s3.amazonaws.com/test?AWSAccessKeyId=AKIAIUTN5UDAA36D3RLQ&Expires="));
    }

    @Test
    public void testToTorrentUrl() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                new Credentials("anonymous", null)));
        assertEquals(new DescriptiveUrl(URI.create("http://test.cyberduck.ch.s3.amazonaws.com/test?torrent"), DescriptiveUrl.Type.torrent),
                new S3UrlProvider(session).get(new Path("/test.cyberduck.ch/test", Path.FILE_TYPE)).find(DescriptiveUrl.Type.torrent));
    }
}