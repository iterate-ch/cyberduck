package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathRelativizerTest {

    @Test
    public void testRelativize() throws Exception {
        assertEquals("a", PathRelativizer.relativize("/", "/a"));
        assertEquals("/b/path", PathRelativizer.relativize("/a", "/b/path"));
        assertEquals("path", PathRelativizer.relativize("/a", "/a/path"));
        assertEquals("a/path", PathRelativizer.relativize("public_html", "/home/user/public_html/a/path"));
        assertEquals("/home/user/public_html/a/path", PathRelativizer.relativize(null, "/home/user/public_html/a/path"));
    }
}
