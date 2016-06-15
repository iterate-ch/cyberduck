package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathNormalizerTest {

    @Test
    public void testNormalize() throws Exception {
        assertEquals(PathNormalizer.normalize("relative/path", false), "relative/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", true), "/absolute/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", false), "/absolute/path");
    }

    @Test
    public void test972() throws Exception {
        assertEquals("//home/path", PathNormalizer.normalize("//home/path"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("p", PathNormalizer.name("/p"));
        assertEquals("n", PathNormalizer.name("/p/n"));
        assertEquals("p", PathNormalizer.name("p"));
        assertEquals("n", PathNormalizer.name("p/n"));
    }

    @Test
    public void testParent() throws Exception {
        assertEquals("/", PathNormalizer.parent("/p", '/'));
        assertEquals("/p", PathNormalizer.parent("/p/n", '/'));
        assertEquals(null, PathNormalizer.parent("/", '/'));
    }

    @Test
    public void testDoubleDot() throws Exception {
        assertEquals("/", PathNormalizer.normalize("/.."));
        assertEquals("/p", PathNormalizer.normalize("/p/n/.."));
        assertEquals("/n", PathNormalizer.normalize("/p/../n"));
        assertEquals("/", PathNormalizer.normalize(".."));
        assertEquals("/", PathNormalizer.normalize("."));
    }

    @Test
    public void testDot() throws Exception {
        assertEquals("/p", PathNormalizer.normalize("/p/."));
        assertEquals("/", PathNormalizer.normalize("/."));
    }
}
