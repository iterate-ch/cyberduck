package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class GroupComparatorTest {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0,
                new GroupComparator(true).compareFirst(new Path("/a", EnumSet.of(Path.Type.file)), new Path("/b", EnumSet.of(Path.Type.file))));
        final Path p = new Path("/a", EnumSet.of(Path.Type.file));
        p.attributes().setGroup("g");
        assertEquals(1,
                new GroupComparator(true).compareFirst(p, new Path("/b", EnumSet.of(Path.Type.file))));
        assertEquals(-1,
                new GroupComparator(true).compareFirst(new Path("/b", EnumSet.of(Path.Type.file)), p));
    }
}
