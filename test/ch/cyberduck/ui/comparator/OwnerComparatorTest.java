package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class OwnerComparatorTest extends AbstractTestCase {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0,
                new OwnerComparator(true).compareFirst(new Path("/a", Path.FILE_TYPE), new Path("/b", Path.FILE_TYPE)));
        final Path p = new Path("/a", Path.FILE_TYPE);
        p.attributes().setOwner("o");
        assertEquals(1,
                new OwnerComparator(true).compareFirst(p, new Path("/b", Path.FILE_TYPE)));
        assertEquals(-1,
                new OwnerComparator(true).compareFirst(new Path("/b", Path.FILE_TYPE), p));
    }
}
