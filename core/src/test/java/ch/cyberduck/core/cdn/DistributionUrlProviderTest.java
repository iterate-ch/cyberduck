package ch.cyberduck.core.cdn;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.net.URI;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DistributionUrlProviderTest {

    @Test
    public void testDownload() {
        final Distribution distribution = new Distribution(Distribution.DOWNLOAD, "n", URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), true);
        final DescriptiveUrl url = new DistributionUrlProvider(distribution).toUrl(
            new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin);
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/p/f", url.getUrl());
        assertEquals("n Origin URL", url.getHelp());
    }

    @Test
    public void testStreaming() {
        final Distribution distribution = new Distribution(Distribution.STREAMING, "n", URI.create("https://test.cyberduck.ch.s3.amazonaws.com"), true);
        distribution.setUrl(URI.create("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st"));
        final DescriptiveUrl cdn = new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.cdn);
        assertEquals("rtmp://d1f6cbdjcbzyiu.cloudfront.net/cfx/st/p/f", cdn.getUrl());
        assertEquals("n Streaming (RTMP) CDN URL", cdn.getHelp());
        final DescriptiveUrl origin = new DistributionUrlProvider(distribution).toUrl(new Path("/test.cyberduck.ch/p/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.origin);
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/p/f", origin.getUrl());
        assertEquals("n Origin URL", origin.getHelp());
    }

    @Test
    public void testCustomOrigin() {
        final Distribution distribution = new Distribution(Distribution.CUSTOM, "n", URI.create("http://test.cyberduck.ch/"), true);
        final DescriptiveUrl url = new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.origin);
        assertEquals("http://test.cyberduck.ch/p/f", url.getUrl());
        assertEquals("n Origin URL", url.getHelp());
    }

    @Test
    public void testCustomOriginDefaultPath() {
        final Distribution distribution = new Distribution(Distribution.CUSTOM, "n", URI.create("http://test.cyberduck.ch/p"), true);
        final DescriptiveUrl url = new DistributionUrlProvider(distribution).toUrl(new Path("/p/f", EnumSet.of(Path.Type.directory))).find(DescriptiveUrl.Type.origin);
        assertEquals("http://test.cyberduck.ch/f", url.getUrl());
        assertEquals("n Origin URL", url.getHelp());
    }
}
