package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class DefaultPathKindDetectorTest {

    @Test
    public void testDetect() throws Exception {
        DefaultPathKindDetector d = new DefaultPathKindDetector();
        assertEquals(Path.FILE_TYPE, d.detect(null));
        assertEquals(Path.DIRECTORY_TYPE, d.detect("/"));
        assertEquals(Path.DIRECTORY_TYPE, d.detect("/a"));
        assertEquals(Path.DIRECTORY_TYPE, d.detect("/a/"));
        assertEquals(Path.FILE_TYPE, d.detect("/a/b.z"));
        assertEquals(Path.FILE_TYPE, d.detect("/a/b.zip"));
    }
}
