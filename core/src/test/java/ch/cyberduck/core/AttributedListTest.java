package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AttributedListTest {

    @Test
    public void testPostFilter() {
        final AttributedList<Path> list = new AttributedList<>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.add(a));
        assertTrue(list.filter(new NullComparator<>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertFalse(list.filter(new NullComparator<>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("b");
            }
        }).isEmpty());
    }

    @Test
    public void testPreFilter() {
        final AttributedList<Path> list = new AttributedList<>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.filter(new NullComparator<>(), new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !file.getName().equals("a");
            }
        }).isEmpty());
        assertTrue(list.add(a));
    }

    @Test
    public void testEquals() {
        final AttributedList<Path> list1 = new AttributedList<>();
        final AttributedList<Path> list2 = new AttributedList<>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list1.add(a));
        assertTrue(list2.add(a));
        assertEquals(list1, list2);
    }

    @Test
    public void testFilterFind() {
        final AttributedList<Path> list = new AttributedList<>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.add(a));
        final AttributedList<Path> filtered1 = list.filter(new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return false;
            }

            @Override
            public Pattern toPattern() {
                return null;
            }
        });
        assertNull(filtered1.find(new SimplePathPredicate(a)));
        final AttributedList<Path> filtered2 = list.filter(new NullFilter<>());
        assertNotNull(filtered2.find(new SimplePathPredicate(a)));
    }

    @Test
    public void testNullFilter() {
        final AttributedList<Path> list = new AttributedList<>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        assertTrue(list.add(a));
        assertNotSame(list, list.filter(new NullFilter<>()));
        assertEquals(list, list.filter(new NullFilter<>()));
    }
}
