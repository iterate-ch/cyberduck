package ch.cyberduck.core;

import ch.cyberduck.core.io.BandwidthThrottle;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * @version $Id:$
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
        final Path file = new Path("name", Path.FILE_TYPE) {
            @Override
            protected AttributedList<Path> list(final AttributedList<Path> children) {
                return AttributedList.emptyList();
            }

            @Override
            public Session getSession() {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream read(final boolean check) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check, final boolean quarantine) {
                throw new UnsupportedOperationException();
            }

            @Override
            public OutputStream write(final boolean check) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void upload(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void mkdir() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void rename(final AbstractPath renamed) {
                throw new UnsupportedOperationException();
            }
        };
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
        assertTrue(cache.containsKey(new PathReference() {
            @Override
            public Object unique() {
                return u;
            }
        }));
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
    }
}
