package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CacheTest extends AbstractTestCase {

    @Test
    public void testLookup() throws Exception {
        Cache cache = new Cache();
        assertNull(cache.lookup(new PathReference() {
            @Override
            public Object unique() {
                return new Object();
            }
        }));
        final Object u = new Object();
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path file = new Path("name", Path.FILE_TYPE);
        list.add(file);
        cache.put(new PathReference() {
            @Override
            public Object unique() {
                return u;
            }
        }, list);
        assertNotNull(cache.lookup(PathReferenceFactory.createPathReference(file)));
    }

    @Test
    public void testIsEmpty() throws Exception {
        Cache cache = new Cache();
        assertTrue(cache.isEmpty());
        cache.put(new PathReference() {
            @Override
            public Object unique() {
                return new Object();
            }
        }, new AttributedList<Path>());
        assertFalse(cache.isEmpty());
    }

    @Test
    public void testContainsKey() throws Exception {
        Cache cache = new Cache();
        final Object u = new Object();
        assertFalse(cache.containsKey(new PathReference() {
            @Override
            public Object unique() {
                return u;
            }
        }));
        cache.put(new PathReference() {
            @Override
            public Object unique() {
                return u;
            }
        }, new AttributedList<Path>());
        final PathReference reference = new PathReference() {
            @Override
            public Object unique() {
                return u;
            }
        };
        assertTrue(cache.containsKey(reference));
        assertTrue(cache.isCached(reference));
    }

    @Test
    public void testInvalidate() throws Exception {
        Cache cache = new Cache();
        final AttributedList<Path> list = new AttributedList<Path>();
        final PathReference reference = new PathReference() {
            final Object o = new Object();

            @Override
            public Object unique() {
                return o;
            }
        };
        cache.put(reference, list);
        assertFalse(cache.get(reference).attributes().isInvalid());
        cache.invalidate(reference);
        assertTrue(cache.get(reference).attributes().isInvalid());
        assertTrue(cache.containsKey(reference));
        assertFalse(cache.isCached(reference));
    }

    @Test
    public void testGet() throws Exception {
        Cache cache = new Cache();
        final Path file = new Path("name", Path.FILE_TYPE);
        assertEquals(AttributedList.<Path>emptyList(), cache.get(file.getReference()));
    }
}