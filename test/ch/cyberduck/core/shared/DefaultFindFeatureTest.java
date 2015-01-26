package ch.cyberduck.core.shared;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.test.NullSession;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class DefaultFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFind() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final DefaultFindFeature feature = new DefaultFindFeature(new NullSession(new Host("t")) {
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
        final DefaultFindFeature feature = new DefaultFindFeature(new NullSession(new Host("t")) {
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
}
