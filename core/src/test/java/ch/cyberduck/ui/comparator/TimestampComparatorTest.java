package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class TimestampComparatorTest {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0, new TimestampComparator(true).compareFirst(new Path("/a", EnumSet.of(Path.Type.file)), new Path("/b", EnumSet.of(Path.Type.file))));
        final Path p1 = new Path("/a", EnumSet.of(Path.Type.file));
        p1.attributes().setModificationDate(System.currentTimeMillis());
        final Path p2 = new Path("/b", EnumSet.of(Path.Type.file));
        p2.attributes().setModificationDate(System.currentTimeMillis() - 1000);
        assertEquals(1, new TimestampComparator(true).compareFirst(p1, p2));
    }
}
