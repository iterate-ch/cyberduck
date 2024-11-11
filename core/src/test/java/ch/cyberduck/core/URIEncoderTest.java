package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URIEncoderTest {

    @Test
    public void testEncode() {
        assertEquals("/p", URIEncoder.encode("/p"));
        assertEquals("/p%20d", URIEncoder.encode("/p d"));
    }

    @Test
    public void testDecode() {
        assertEquals("/p", URIEncoder.decode("/p"));
        assertEquals("/%0", URIEncoder.decode("/%250"));
    }

    @Test
    public void testEncodeHash() {
        assertEquals("file%23", URIEncoder.encode("file#"));
    }

    @Test
    public void testEncodeTrailingDelimiter() {
        assertEquals("/a/p/", URIEncoder.encode("/a/p/"));
        assertEquals("/p%20d/", URIEncoder.encode("/p d/"));
    }

    @Test
    public void testEncodeRelativeUri() {
        assertEquals("a/p", URIEncoder.encode("a/p"));
        assertEquals("a/p/", URIEncoder.encode("a/p/"));
    }

    @Test
    public void testEncodeEmoji() {
        assertEquals("a/%F0%9F%9A%80", URIEncoder.encode("a/\uD83D\uDE80"));
        assertEquals("a/\uD83D\uDE80", URIEncoder.decode("a/%F0%9F%9A%80"));
    }
}
