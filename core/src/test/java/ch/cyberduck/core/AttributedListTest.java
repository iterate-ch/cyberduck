package ch.cyberduck.core;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AttributedListTest {

    @Test
    public void testPostFilter() throws Exception {
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.add(a));
        assertTrue(list.filter(new NullComparator<Path>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertEquals(Collections.singletonList(a), list.attributes().getHidden());
        assertFalse(list.filter(new NullComparator<Path>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("b");
            }
        }).isEmpty());
        assertEquals(Collections.<Path>emptyList(), list.attributes().getHidden());
    }

    @Test
    public void testPreFilter() throws Exception {
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.filter(new NullComparator<Path>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertTrue(list.add(a));
        assertEquals(Collections.singletonList(a), list.attributes().getHidden());
    }

    @Test
    public void testEquals() throws Exception {
        final AttributedList<Path> list1 = new AttributedList<Path>();
        final AttributedList<Path> list2 = new AttributedList<Path>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list1.add(a));
        assertTrue(list2.add(a));
        assertEquals(list1, list2);
    }

    @Test
    public void testFilterFind() throws Exception {
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.add(a));
        list.filter(new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return false;
            }

            @Override
            public Pattern toPattern() {
                return null;
            }
        });
        assertNull(list.find(new SimplePathPredicate(a)));
        list.filter(new NullFilter<>());
        assertNotNull(list.find(new SimplePathPredicate(a)));
    }
}
