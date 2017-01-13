package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MappingMimeTypeServiceTest {

    @Test
    public void testGetMime() throws Exception {
        MappingMimeTypeService s = new MappingMimeTypeService();
        assertEquals("text/plain", s.getMime("f.txt"));
        assertEquals("text/plain", s.getMime("f.TXT"));
        assertEquals("video/x-f4v", s.getMime("f.f4v"));
        assertEquals("application/javascript", s.getMime("f.js"));
        assertEquals("video/mp2t", s.getMime("f.ts"));
        assertEquals("application/x-mpegurl", s.getMime("f.m3u8"));
        assertEquals("application/octet-stream", s.getMime("._f.txt"));
    }
}
