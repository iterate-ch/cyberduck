package ch.cyberduck.core.worker;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.pool.StatelessSessionPool;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class SessionListWorkerTest {

    @Test
    public void testRun() throws Exception {
        final Host host = new Host(new TestProtocol());
        final Session<?> session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws NotfoundException {
                return new AttributedList<>(Collections.singletonList(new Path("/home/jenkins/f", EnumSet.of(Path.Type.file))));
            }
        };
        final PathCache cache = new PathCache(1);
        final SessionListWorker worker = new SessionListWorker(cache,
                new Path("/home/jenkins", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        final AttributedList<Path> list = worker.run(session);
        assertFalse(list.isEmpty());
        assertFalse(cache.containsKey(new Path("/home/jenkins", EnumSet.of(Path.Type.directory))));
        worker.cleanup(list);
        assertTrue(cache.containsKey(new Path("/home/jenkins", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testCacheNotFoundWithController() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost");
        final Session<?> session = new NullSession(host);
        final PathCache cache = new PathCache(1);
        final SessionListWorker worker = new SessionListWorker(cache,
                new Path("/home/notfound", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        final Controller c = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final Future<AttributedList<Path>> task = c.background(new WorkerBackgroundAction<AttributedList<Path>>(c, new StatelessSessionPool(
                new TestLoginConnectionService(), session, PathCache.empty(),
                new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())), worker));
        assertTrue(task.get().isEmpty());
        assertTrue(cache.containsKey(new Path("/home/notfound", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testCacheListCanceledWithController() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost");
        final Session<?> session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
                throw new ListCanceledException(AttributedList.<Path>emptyList());
            }
        };
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/home/notfound", EnumSet.of(Path.Type.directory));
        cache.put(directory, new AttributedList<>(Collections.singletonList(new Path(directory, "f", EnumSet.of(Path.Type.file)))));
        final SessionListWorker worker = new SessionListWorker(cache,
                directory,
                new DisabledListProgressListener());
        final Controller c = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final Future<AttributedList<Path>> task = c.background(new WorkerBackgroundAction<AttributedList<Path>>(c, new StatelessSessionPool(
                new TestLoginConnectionService(), session, PathCache.empty(),
                new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())), worker));
        assertNotNull(task.get());
        assertTrue(cache.containsKey(directory));
        assertEquals(1, cache.get(directory).size());
    }

    @Test
    public void testInitialValueOnFailure() throws Exception {
        final SessionListWorker worker = new SessionListWorker(PathCache.empty(),
                new Path("/home/notfound", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertSame(AttributedList.emptyList(), worker.initialize());
    }
}
