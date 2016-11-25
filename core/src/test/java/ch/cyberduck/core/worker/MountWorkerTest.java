package ch.cyberduck.core.worker;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class MountWorkerTest {

    @Test
    public void testRunInvalidDefaultPath() throws Exception {
        final Host host = new Host(new TestProtocol());
        host.setDefaultPath("/notfound");
        final Session<?> session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws NotfoundException {
                if(file.equals(new Path("/notfound", EnumSet.of(Path.Type.volume, Path.Type.directory)))) {
                    throw new NotfoundException("");
                }
                return new AttributedList<>(Collections.emptyList());
            }
        };
        final Cache<Path> cache = new PathCache(2);
        final MountWorker worker = new MountWorker(host, cache, new DisabledListProgressListener());
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), worker.run(session));
        assertTrue(cache.containsKey(new Path("/", EnumSet.of(Path.Type.directory))));
        assertFalse(cache.containsKey(new Path("/notfound", EnumSet.of(Path.Type.directory))));
        session.close();
    }
}
