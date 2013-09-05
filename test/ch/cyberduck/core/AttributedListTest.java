package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AttributedListTest extends AbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        assertTrue(list.add(new Path("/a", Path.DIRECTORY_TYPE)));
        assertTrue(list.contains(new NSObjectPathReference(NSString.stringWithString("/a-2"))));
    }

    @Test
    public void testRemove() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        assertTrue(list.add(new Path("/a", Path.DIRECTORY_TYPE)));
        assertTrue(list.contains(new NSObjectPathReference(NSString.stringWithString("/a-2"))));
        list.remove(0);
        assertFalse(list.contains(new NSObjectPathReference(NSString.stringWithString("/a-2"))));
    }

    @Test
    public void testFilter() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        final Path a = new Path("/a", Path.DIRECTORY_TYPE);
        assertTrue(list.add(a));
        assertTrue(list.filter(new NullComparator(), new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertEquals(Collections.<Path>singletonList(a), list.attributes().getHidden());
        assertFalse(list.filter(new NullComparator(), new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("b");
            }
        }).isEmpty());
        assertEquals(Collections.<Path>emptyList(), list.attributes().getHidden());
    }
}
