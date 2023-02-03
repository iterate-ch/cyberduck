package ch.cyberduck.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionTest {

    @Test
    public void testClear() {
        Collection<Object> c = new Collection<>();
        c.add(new Object());
        c.clear();
        assertTrue(c.isEmpty());
    }

    @Test
    public void testRemoveAll() {
        Collection<Object> collection = new Collection<>();
        final Object a = new Object();
        collection.add(a);
        final Object b = new Object();
        collection.add(b);
        final Object c = new Object();
        collection.add(c);
        collection.removeAll(Arrays.asList(a, b));
        assertFalse(collection.contains(a));
        assertFalse(collection.contains(b));
        assertTrue(collection.contains(c));
    }

    @Test
    public void testRemoveIterator() {
        Collection<Object> collection = new Collection<>();
        final Object a = new Object();
        collection.add(a);
        final Object b = new Object();
        collection.add(b);
        assertFalse(collection.isEmpty());
        for(Iterator<Object> iter = collection.iterator(); iter.hasNext(); ) {
            iter.next();
            iter.remove();
        }
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testRemoveLambda() {
        Collection<Object> collection = new Collection<>();
        final Object a = new Object();
        collection.add(a);
        final Object b = new Object();
        collection.add(b);
        assertFalse(collection.isEmpty());
        collection.removeIf(Objects::nonNull);
        assertTrue(collection.isEmpty());
    }
}
