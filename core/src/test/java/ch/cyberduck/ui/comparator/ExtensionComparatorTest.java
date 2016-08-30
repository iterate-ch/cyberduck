package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class ExtensionComparatorTest {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0,
                new ExtensionComparator(true).compareFirst(new Path("/a.a", EnumSet.of(Path.Type.directory)), new Path("/b.b", EnumSet.of(Path.Type.directory))));
        assertEquals(0,
                new ExtensionComparator(true).compareFirst(new Path("/a", EnumSet.of(Path.Type.file)), new Path("/b", EnumSet.of(Path.Type.file))));
        assertEquals(1,
                new ExtensionComparator(true).compareFirst(new Path("/a.txt", EnumSet.of(Path.Type.file)), new Path("/b", EnumSet.of(Path.Type.file))));
        assertEquals(0,
                new ExtensionComparator(true).compareFirst(new Path("/a.txt", EnumSet.of(Path.Type.file)), new Path("/b.txt", EnumSet.of(Path.Type.file))));
        assertEquals(-1,
                new ExtensionComparator(true).compareFirst(new Path("/a", EnumSet.of(Path.Type.file)), new Path("/b.txt", EnumSet.of(Path.Type.file))));
    }
}
