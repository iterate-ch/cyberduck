package ch.cyberduck.ui.comparator;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class SizeComparatorTest extends AbstractTestCase {

    @Test
    public void testCompareFirst() throws Exception {
        assertEquals(0,
                new ExtensionComparator(true).compareFirst(new Path("/a", Path.FILE_TYPE), new Path("/b", Path.FILE_TYPE)));
    }
}
