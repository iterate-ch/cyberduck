package ch.cyberduck.core;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionTest {

    @Test
    public void testClear() throws Exception {
        Collection<Object> c = new Collection<Object>();
        c.add(new Object());
        c.clear();
        assertTrue(c.isEmpty());
    }

    @Test
    public void testRemoveAll() throws Exception {
        Collection<Object> collection = new Collection<Object>();
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
}
