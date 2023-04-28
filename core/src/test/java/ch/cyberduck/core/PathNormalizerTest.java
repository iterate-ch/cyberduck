package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathNormalizerTest {

    @Test
    public void testNormalize() {
        assertEquals(PathNormalizer.normalize("relative/path", false), "relative/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", true), "/absolute/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", false), "/absolute/path");
    }

    @Test
    public void test972() {
        assertEquals("//home/path", PathNormalizer.normalize("//home/path"));
    }

    @Test
    public void testName() {
        assertEquals("p", PathNormalizer.name("/p"));
        assertEquals("n", PathNormalizer.name("/p/n"));
        assertEquals("p", PathNormalizer.name("p"));
        assertEquals("n", PathNormalizer.name("p/n"));
    }

    @Test
    public void testParent() {
        assertEquals("/", PathNormalizer.parent("/p", '/'));
        assertEquals("/p", PathNormalizer.parent("/p/n", '/'));
        assertNull(PathNormalizer.parent("/", '/'));
    }

    @Test
    public void testDoubleDot() {
        assertEquals("/", PathNormalizer.normalize("/.."));
        assertEquals("/p", PathNormalizer.normalize("/p/n/.."));
        assertEquals("/n", PathNormalizer.normalize("/p/../n"));
        assertEquals("/", PathNormalizer.normalize(".."));
        assertEquals("/", PathNormalizer.normalize("."));
    }

    @Test
    public void testDot() {
        assertEquals("/p", PathNormalizer.normalize("/p/."));
        assertEquals("/", PathNormalizer.normalize("/."));
    }

    @Test
    public void testPathNormalize() {
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/.."), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/.././"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/remove/../to/remove/.././"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/remove/../../"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/././././to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "./.path/to"), EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                ".path/to"), EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/.to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/.to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path//to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path///to////"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
    }

    @Test
    public void testPathName() {
        {
            Path path = new Path(PathNormalizer.normalize(
                "/path/to/file/"), EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = new Path(PathNormalizer.normalize(
                "/path/to/file"), EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    @Test
    public void testNormalizeNameWithBackslash() {
        assertEquals("file\\name", PathNormalizer.name("/path/to/file\\name"));
    }

    @Test
    public void testFindWithWorkdir() {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }

    @Test
    public void testRelativeParent() {
        final Path home = PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox/sub");
        assertEquals(new Path("/sandbox/sub", EnumSet.of(Path.Type.directory)), home);
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)), home.getParent());
    }

    @Test
    public void testHomeParent() {
        final Path home = PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), String.format("%s/sandbox/sub", Path.HOME));
        assertEquals(new Path("/sandbox/sub", EnumSet.of(Path.Type.directory)), home);
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)), home.getParent());
    }

    @Test
    public void testStartingWithHome() {
        final Path home = PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), String.format("%smyfolder/sub", Path.HOME));
        assertEquals(new Path(String.format("/%smyfolder/sub", Path.HOME), EnumSet.of(Path.Type.directory)), home);
        assertEquals(new Path(String.format("/%smyfolder", Path.HOME), EnumSet.of(Path.Type.directory)), home.getParent());
    }

    @Test
    public void testDefaultLocalPathDriveLetter() {
        assertEquals(new Path("/C:/Users/example/Documents/vault", EnumSet.of(Path.Type.directory)),
                PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), "C:/Users/example/Documents/vault"));
    }

    @Test
    public void testDefaultLocalPathDriveLetterBackwardSlashes() {
        assertEquals(new Path("/C:/Users/example/Documents/vault", EnumSet.of(Path.Type.directory)),
            PathNormalizer.compose(new Path("/", EnumSet.of(Path.Type.directory)), "C:\\Users\\example\\Documents\\vault"));
    }
}
