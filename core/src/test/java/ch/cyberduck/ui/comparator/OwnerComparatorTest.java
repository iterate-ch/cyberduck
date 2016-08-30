package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class OwnerComparatorTest {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0,
                new OwnerComparator(true).compareFirst(new Path("/a", EnumSet.of(Path.Type.file)), new Path("/b", EnumSet.of(Path.Type.file))));
        final Path p = new Path("/a", EnumSet.of(Path.Type.file));
        p.attributes().setOwner("o");
        assertEquals(1,
                new OwnerComparator(true).compareFirst(p, new Path("/b", EnumSet.of(Path.Type.file))));
        assertEquals(-1,
                new OwnerComparator(true).compareFirst(new Path("/b", EnumSet.of(Path.Type.file)), p));
    }
}
