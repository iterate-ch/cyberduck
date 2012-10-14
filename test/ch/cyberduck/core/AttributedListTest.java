package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class AttributedListTest extends AbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        assertTrue(list.add(new NullPath("/a", Path.DIRECTORY_TYPE)));
        assertTrue(list.contains(new NSObjectPathReference(NSString.stringWithString("/a"))));
    }

    @Test
    public void testFilter() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        assertTrue(list.add(new NullPath("/a", Path.DIRECTORY_TYPE)));
        assertTrue(list.filter(new NullComparator(), new PathFilter() {
            @Override
            public boolean accept(final AbstractPath file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertFalse(list.filter(new NullComparator(), new PathFilter() {
            @Override
            public boolean accept(final AbstractPath file) {
                return !file.getName().equals("b");
            }
        }).isEmpty());
    }
}
