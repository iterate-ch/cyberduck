package ch.cyberduck.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CacheTest extends AbstractTestCase {

    private Cache cache;

    @Before
    public void setup() {
        cache = new Cache();
    }

    @Test
    public void testLookup() throws Exception {
        assertNull(cache.lookup(new PathReference() {
            @Override
            public Object unique() {
                return new Object();
            }
        }));
        final Object u = new Object();
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path file = new NullPath("name", Path.FILE_TYPE);
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
}