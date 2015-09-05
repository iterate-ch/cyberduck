package ch.cyberduck.core.worker;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SessionListWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
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
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
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
        final Future<AttributedList<Path>> task = c.background(new WorkerBackgroundAction<AttributedList<Path>>(c, session, worker));
        assertNull(task.get());
        assertTrue(cache.containsKey(new Path("/home/notfound", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testCacheListCanceledWithController() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host) {
            @Override
            public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
                throw new ListCanceledException(AttributedList.<Path>emptyList());
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
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
        final Future<AttributedList<Path>> task = c.background(new WorkerBackgroundAction<AttributedList<Path>>(c, session, worker));
        assertNotNull(task.get());
        assertTrue(cache.containsKey(new Path("/home/notfound", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testInitialValueOnFailure() throws Exception {
        final SessionListWorker worker = new SessionListWorker(PathCache.empty(),
                new Path("/home/notfound", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertSame(AttributedList.emptyList(), worker.initialize());
    }
}
