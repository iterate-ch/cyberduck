package ch.cyberduck.core.cdn;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.net.URI;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DistributionUrlProviderTest {

    @Test
    public void testDownload() throws Exception {
        final Distribution distribution = new Distribution(URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), Distribution.DOWNLOAD, true);
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/p/f",
                new DistributionUrlProvider(distribution).toUrl(
                        new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin).getUrl());
    }

    @Test
    public void testStreaming() throws Exception {
        final Distribution distribution = new Distribution(URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), Distribution.STREAMING, true);
        distribution.setUrl(URI.create("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st"));
        assertEquals("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st/p/f",
                new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.cdn).getUrl());
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/p/f",
                new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin).getUrl());
    }

    @Test
    public void testCustomOrigin() throws Exception {
        final Distribution distribution = new Distribution(URI.create("http://test.cyberduck.ch/"), Distribution.CUSTOM, true);
        assertEquals("http://test.cyberduck.ch/p/f",
                new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.origin).getUrl());
    }

    @Test
    public void testCustomOriginDefaultPath() throws Exception {
        final Distribution distribution = new Distribution(URI.create("http://test.cyberduck.ch/p"), Distribution.CUSTOM, true);
        assertEquals("http://test.cyberduck.ch/f",
                new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.origin).getUrl());
    }
}
