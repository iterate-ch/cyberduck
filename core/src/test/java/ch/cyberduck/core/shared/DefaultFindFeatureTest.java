package ch.cyberduck.core.shared;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DefaultFindFeatureTest {

    @Test
    public void testFind() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final DefaultFindFeature feature = new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(Path file, ListProgressListener listener) {
                count.incrementAndGet();
                return AttributedList.emptyList();
            }
        });
        assertFalse(feature.find(new Path("/t", EnumSet.of(Path.Type.directory))));
        assertEquals(1, count.get());
        assertFalse(feature.find(new Path("/t", EnumSet.of(Path.Type.directory))));
        assertEquals(2, count.get());
    }

    @Test
    public void testFindCached() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final DefaultFindFeature feature = new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(Path file, ListProgressListener listener) {
                count.incrementAndGet();
                return AttributedList.emptyList();
            }
        }).withCache(new PathCache(2));
        assertFalse(feature.find(new Path("/t", EnumSet.of(Path.Type.directory))));
        assertEquals(1, count.get());
        assertFalse(feature.find(new Path("/t", EnumSet.of(Path.Type.directory))));
        assertEquals(1, count.get());
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        assertTrue(new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/a/b", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
            }
        }).find(new Path("/a/b", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        assertTrue(new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public Case getCase() {
                return Case.insensitive;
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/a/B", EnumSet.of(Path.Type.file))));
            }
        }).find(new Path("/a/b", EnumSet.of(Path.Type.file))));
        assertFalse(new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public Case getCase() {
                return Case.insensitive;
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/a/B", EnumSet.of(Path.Type.directory))));
            }
        }).find(new Path("/a/b", EnumSet.of(Path.Type.file))));
        assertFalse(new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public Case getCase() {
                return Case.sensitive;
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/a/B", EnumSet.of(Path.Type.file))));
            }
        }).find(new Path("/a/b", EnumSet.of(Path.Type.file))));
    }
}
