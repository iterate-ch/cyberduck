package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class TimestampComparatorTest extends AbstractTestCase {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0, new TimestampComparator(true).compareFirst(new Path("/a", Path.FILE_TYPE), new Path("/b", Path.FILE_TYPE)));
        final Path p1 = new Path("/a", Path.FILE_TYPE);
        p1.attributes().setModificationDate(System.currentTimeMillis());
        final Path p2 = new Path("/b", Path.FILE_TYPE);
        p2.attributes().setModificationDate(System.currentTimeMillis() - 1000);
        assertEquals(1, new TimestampComparator(true).compareFirst(p1, p2));
    }
}
