package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultPathKindDetectorTest {

    @Test
    public void testDetect() throws Exception {
        DefaultPathKindDetector d = new DefaultPathKindDetector();
        assertEquals(Path.Type.directory, d.detect(null));
        assertEquals(Path.Type.directory, d.detect("/"));
        assertEquals(Path.Type.directory, d.detect("/a"));
        assertEquals(Path.Type.directory, d.detect("/a/"));
        assertEquals(Path.Type.file, d.detect("/a/b.z"));
        assertEquals(Path.Type.file, d.detect("/a/b.zip"));
    }
}
