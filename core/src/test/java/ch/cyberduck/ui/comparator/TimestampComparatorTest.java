package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.AbstractPath;
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

    @Test
    public void testCompareTransitivity() throws Exception {
        final BrowserComparator comparator = new TimestampComparator(true);
        final Path p1 = new Path("/c", EnumSet.of(Path.Type.file));
        p1.attributes().setModificationDate(1000);
        final Path p2 = new Path("/b", EnumSet.of(Path.Type.directory));
        p2.attributes().setModificationDate(-1);
        assertEquals(1, comparator.compare(p1, p2));
        final Path p3 = new Path("/a", EnumSet.of(Path.Type.directory));
        p3.attributes().setModificationDate(-1);
        assertEquals(1, comparator.compare(p2, p3));
        assertEquals(1, comparator.compare(p1, p3));
    }
}
