package ch.cyberduck.core.cdn;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DistributionUrlProviderTest extends AbstractTestCase {

    @Test
    public void testDownload() throws Exception {
        final Distribution distribution = new Distribution(URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), Distribution.DOWNLOAD, true);
        assertEquals(new DescriptiveUrl(URI.create("https://test.cyberduck.ch.s3.amazonaws.com/p/f")),
                new DistributionUrlProvider(distribution).toUrl(
                        new Path("/test.cyberduck.ch/p/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.origin));
    }

    @Test
    public void testStreaming() throws Exception {
        final Distribution distribution = new Distribution(URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), Distribution.STREAMING, true);
        distribution.setUrl(URI.create("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st"));
        assertEquals(new DescriptiveUrl(URI.create("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st/p/f")),
                new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.cdn));
        assertEquals(new DescriptiveUrl(URI.create("https://test.cyberduck.ch.s3.amazonaws.com/p/f")),
                new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.origin));
    }

    @Test
    public void testCustomOrigin() throws Exception {
        final Distribution distribution = new Distribution(URI.create("http://test.cyberduck.ch/"), Distribution.CUSTOM, true);
        assertEquals(new DescriptiveUrl(URI.create("http://test.cyberduck.ch/p/f")),
                new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.origin));
    }

    @Test
    public void testCustomOriginDefaultPath() throws Exception {
        final Distribution distribution = new Distribution(URI.create("http://test.cyberduck.ch/p"), Distribution.CUSTOM, true);
        assertEquals(new DescriptiveUrl(URI.create("http://test.cyberduck.ch/f")),
                new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.origin));
    }
}
