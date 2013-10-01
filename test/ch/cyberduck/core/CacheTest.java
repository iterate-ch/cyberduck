package ch.cyberduck.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CacheTest extends AbstractTestCase {

    @Test
    public void testLookup() throws Exception {
        Cache cache = new Cache();
        assertNull(cache.lookup(new DefaultPathReference(new Path("/", Path.DIRECTORY_TYPE))));
        final Object u = new Object();
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path file = new Path("name", Path.FILE_TYPE);
        list.add(file);
        cache.put(new PathReference() {
            @Override
            public Object unique() {
                return u;
            }

            @Override
            public String attributes() {
                return StringUtils.EMPTY;
            }
        }, list);
        assertNotNull(cache.lookup(PathReferenceFactory.createPathReference(file)));
    }

    @Test
    public void testIsHidden() throws Exception {
        Cache cache = new Cache();
        final Path parent = new Path("/", Path.DIRECTORY_TYPE);
        final AttributedList<Path> list = new AttributedList<Path>(
                Arrays.asList(new Path(parent, "a", Path.FILE_TYPE), new Path(parent, "b", Path.FILE_TYPE)));
        list.filter(new Filter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return file.equals(new Path(parent, "a", Path.FILE_TYPE));
            }
        });
        cache.put(new NSObjectPathReference(parent), list);
        assertFalse(cache.isHidden(new Path(parent, "a", Path.FILE_TYPE)));
        assertTrue(cache.isHidden(new Path(parent, "b", Path.FILE_TYPE)));
    }

    @Test
    public void testIsEmpty() throws Exception {
        Cache cache = new Cache();
        assertTrue(cache.isEmpty());
        cache.put(new DefaultPathReference(new Path("/", Path.DIRECTORY_TYPE)), new AttributedList<Path>());
        assertFalse(cache.isEmpty());
    }

    @Test
    public void testContainsKey() throws Exception {
        Cache cache = new Cache();
        assertFalse(cache.containsKey(new DefaultPathReference(new Path("/", Path.DIRECTORY_TYPE))));
        cache.put(new DefaultPathReference(new Path("/", Path.DIRECTORY_TYPE)), new AttributedList<Path>());
        final PathReference reference = new DefaultPathReference(new Path("/", Path.DIRECTORY_TYPE));
        assertTrue(cache.containsKey(reference));
        assertTrue(cache.isCached(reference));
    }

    @Test
    public void testInvalidate() throws Exception {
        Cache cache = new Cache();
        final AttributedList<Path> list = new AttributedList<Path>();
        final PathReference reference = new DefaultPathReference(new Path("/t", Path.DIRECTORY_TYPE));
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