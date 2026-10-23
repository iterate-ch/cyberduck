package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class PathCacheTest {

    @Test
    public void testIsEmpty() {
        final PathCache cache = new PathCache(1);
        assertTrue(cache.isEmpty());
        cache.put(new Path("/", EnumSet.of(Path.Type.directory)), new AttributedList<>());
        assertFalse(cache.isEmpty());
    }

    @Test
    public void testContainsKey() {
        final PathCache cache = new PathCache(1);
        final Path f = new Path("/", EnumSet.of(Path.Type.directory));
        assertFalse(cache.containsKey(f));
        cache.put(f, new AttributedList<>());
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
    }

    @Test
    public void testInvalidate() {
        final PathCache cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<>();
        final Path f = new Path("/t", EnumSet.of(Path.Type.directory));
        cache.put(f, list);
        assertFalse(cache.get(f).attributes().isInvalid());
        cache.invalidate(f);
        assertTrue(cache.get(f).attributes().isInvalid());
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
        assertFalse(cache.isValid(f));
    }

    @Test
    public void testGet() {
        final PathCache cache = new PathCache(1);
        final Path file = new Path("name", EnumSet.of(Path.Type.file));
        assertEquals(AttributedList.<Path>emptyList(), cache.get(file));
    }

    @Test
    public void testGetNull() {
        final PathCache cache = new PathCache(1);
        final Path file = new Path("name", EnumSet.of(Path.Type.file));
        final AttributedList<Path> list = new AttributedList<>();
        list.add(file);
        cache.put(null, list);
        assertEquals(list, cache.get(null));
    }

    @Test
    public void testDisabledCache() {
        PathCache cache = PathCache.empty();
        final Path file = new Path("name", EnumSet.of(Path.Type.file));
        cache.put(file, AttributedList.emptyList());
        assertFalse(cache.containsKey(file));
        assertEquals(0, cache.size());
    }

    @Test
    public void testDirectoryWithFileIdDefaultPredicate() {
        final PathCache cache = new PathCache(1);
        final Path f = new Path("/", EnumSet.of(Path.Type.directory)).withAttributes(new DefaultPathAttributes().setFileId("1"));
        assertFalse(cache.containsKey(f));
        cache.put(f, new AttributedList<>());
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
        assertFalse(cache.containsKey(new Path("/", EnumSet.of(Path.Type.directory))));
        assertFalse(cache.isCached(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testDirectoryWithFileIdSimplePredicate() {
        final PathCache cache = new PathCache(1) {
            @Override
            public CacheReference<Path> reference(final Path file) {
                return new SimplePathPredicate(file);
            }
        };
        final Path f = new Path("/", EnumSet.of(Path.Type.directory)).withAttributes(new DefaultPathAttributes().setFileId("1"));
        assertFalse(cache.containsKey(f));
        cache.put(f, new AttributedList<>());
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
        assertTrue(cache.containsKey(new Path("/", EnumSet.of(Path.Type.directory))));
        assertTrue(cache.isCached(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testUnlockAfterFileIdResolved() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        cache.lock(directory);
        // Attributes of the directory are changed while looking up the contents, such as the file id resolved when listing
        directory.attributes().setFileId("f");
        cache.unlock(directory);
        // Lock is released
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertTrue(executor.submit(() -> {
                final Path other = new Path("/d", EnumSet.of(Path.Type.directory));
                cache.lock(other);
                cache.unlock(other);
                return true;
            }).get(2, TimeUnit.SECONDS));
        }
        finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testLockExclusiveRegardlessOfFileId() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        cache.lock(directory);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final Future<Boolean> waiting = executor.submit(() -> {
                final Path other = new Path("/d", EnumSet.of(Path.Type.directory));
                other.attributes().setFileId("f");
                cache.lock(other);
                cache.unlock(other);
                return true;
            });
            try {
                waiting.get(300, TimeUnit.MILLISECONDS);
                fail("Expected to wait for lock of the same directory");
            }
            catch(TimeoutException e) {
                // Expected
            }
            cache.unlock(directory);
            assertTrue(waiting.get(2, TimeUnit.SECONDS));
        }
        finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testUnlockNotLocked() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        // Log error only
        cache.unlock(directory);
        cache.lock(directory);
        cache.unlock(directory);
        cache.unlock(directory);
    }

    @Test
    public void testUnlockNotOwner() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        cache.lock(directory);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // Not held by thread. Log error only
            executor.submit(() -> cache.unlock(directory)).get(2, TimeUnit.SECONDS);
        }
        finally {
            executor.shutdownNow();
        }
        // Still locked by owner
        cache.unlock(directory);
    }
}
