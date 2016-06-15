package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URIEncoderTest {

    @Test
    public void testEncode() throws Exception {
        assertEquals("/p", URIEncoder.encode("/p"));
        assertEquals("/p%20d", URIEncoder.encode("/p d"));
    }

    @Test
    public void testEncodeTrailingDelimiter() throws Exception {
        assertEquals("/a/p/", URIEncoder.encode("/a/p/"));
        assertEquals("/p%20d/", URIEncoder.encode("/p d/"));
    }

    @Test
    public void testEncodeRelativeUri() throws Exception {
        assertEquals("a/p", URIEncoder.encode("a/p"));
        assertEquals("a/p/", URIEncoder.encode("a/p/"));
    }
}
