package ch.cyberduck.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

public class PathNormalizerTest {

    @Test
    public void testNormalizeSelection() {
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        final Path ab = new Path("/a/b", EnumSet.of(Path.Type.file));
        final Path abc = new Path("/a/b/c", EnumSet.of(Path.Type.file));
        final Path sibling = new Path("/ab", EnumSet.of(Path.Type.directory));
        final Path f = new Path("/f", EnumSet.of(Path.Type.file));
        final Path fc = new Path("/f/c", EnumSet.of(Path.Type.file));
        // Children of included directory are removed
        assertEquals(Arrays.asList(a, sibling), PathNormalizer.normalize(Arrays.asList(a, ab, abc, sibling)));
        // Only directories included before are considered
        assertEquals(Arrays.asList(ab, a), PathNormalizer.normalize(Arrays.asList(ab, a)));
        // A file has no children
        assertEquals(Arrays.asList(f, fc), PathNormalizer.normalize(Arrays.asList(f, fc)));
        final Path root = new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals(Collections.singletonList(root), PathNormalizer.normalize(Arrays.asList(root, a, ab, f)));
        assertTrue(PathNormalizer.normalize(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testNormalizeLargeSelection() {
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        final List<Path> selected = new ArrayList<>();
        for(int i = 0; i < 100000; i++) {
            selected.add(new Path(directory, String.format("f-%d", i), EnumSet.of(Path.Type.file)));
        }
        assertEquals(selected, PathNormalizer.normalize(selected));
        selected.add(0, directory);
        assertEquals(Collections.singletonList(directory), PathNormalizer.normalize(selected));
    }

    @Test
    public void testNormalize() {
        assertEquals("relative/path", PathNormalizer.normalize("relative/path", false));
        assertEquals("/absolute/path", PathNormalizer.normalize("/absolute/path", true));
        assertEquals("/absolute/path", PathNormalizer.normalize("/absolute/path", false));
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

    @Test
    public void testComposeInvalidName() {
        final Path workdir = new Path("/workdir", EnumSet.of(Path.Type.directory));
        assertSame(workdir, PathNormalizer.compose(workdir, "/"));
        assertSame(workdir, PathNormalizer.compose(workdir, "//"));
        assertSame(workdir, PathNormalizer.compose(workdir, ""));
        assertEquals(new Path(workdir, " ", EnumSet.of(Path.Type.directory)), PathNormalizer.compose(workdir, " "));
    }
}
