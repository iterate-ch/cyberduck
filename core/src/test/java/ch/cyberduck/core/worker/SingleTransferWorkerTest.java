package ch.cyberduck.core.worker;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class SingleTransferWorkerTest {

    @Test
    public void testUploadPrepareOverrideRootDoesNotExist() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory)) {
            @Override
            public Path getParent() {
                return new Path("/", EnumSet.of(Path.Type.directory));
            }
        };
        final NullLocal local = new NullLocal("l") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<>();
                l.add(new NullLocal(this.getAbsolute(), "c") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
                return l;
            }

            @Override
            public boolean exists() {
                return true;
            }
        };
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus segment,
                                 final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) {
                //
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService()) {
            @Override
            public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(this.getCache().isCached(new TransferItem(root, local)));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                }), action);
                assertFalse(this.getCache().isCached(new TransferItem(child, local)));
                return null;
            }
        };
        worker.run(session);
        assertFalse(worker.getCache().isCached(new TransferItem(child, local)));
    }

    @Test
    public void testUploadPrepareOverrideRootExists() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("l") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<>();
                l.add(new NullLocal(this.getAbsolute(), "c") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
                return l;
            }

            @Override
            public boolean exists() {
                return true;
            }
        };
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus segment,
                                 final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) {
                if(file.equals(root)) {
                    assertTrue(segment.isExists());
                }
                else {
                    assertFalse(segment.isExists());
                }
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService()) {
            @Override
            public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(this.getCache().isCached(new TransferItem(root, local)));
                }
                super.transfer(item, action);
                assertFalse(this.getCache().isCached(new TransferItem(child, local)));
                return null;
            }
        };
        worker.run(session);
        assertFalse(worker.getCache().isCached(new TransferItem(child, local)));
        assertTrue(worker.getCache().isEmpty());
    }

    @Test
    public void testDownloadPrepareOverride() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("l") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public AttributedList<Local> list() {
                return AttributedList.emptyList();
            }
        };
        final PathCache cache = new PathCache(2);
        cache.put(root.getParent(), new AttributedList<>(Collections.singletonList(root)));
        cache.put(root, new AttributedList<>(Collections.singletonList(child)));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus segment,
                                 final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) {
                if(file.equals(root)) {
                    assertTrue(segment.isExists());
                }
                else {
                    assertFalse(segment.isExists());
                }
            }
        }.withCache(cache);
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService()) {
            @Override
            public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(this.getCache().isCached(new TransferItem(root, local)));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l")), action);
                if(item.remote.equals(root)) {
                    assertFalse(this.getCache().isCached(new TransferItem(root, local)));
                }
                return null;
            }
        };
        worker.run(session);
        assertFalse(worker.getCache().isCached(new TransferItem(child, local)));
        assertTrue(worker.getCache().isEmpty());
    }

    @Test
    public void testFailedStatusUploadWithLengthOfLocalFile() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        Files.write(Paths.get(local.getAbsolute()), new byte[]{1, 2, 3});
        try {
            final Host bookmark = new Host(new TestProtocol());
            final Transfer t = new UploadTransfer(bookmark, file, local);
            final NullSession session = new NullSession(bookmark);
            final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t),
                    new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                    ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService());
            final TransferStatus status = worker.failed(file, local, new AccessDeniedException());
            assertEquals(3L, status.getLength());
            assertFalse(status.isComplete());
        }
        finally {
            local.delete();
        }
    }

    @Test
    public void testFailedStatusDownloadWithLengthOfRemoteFile() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        file.attributes().setSize(5L);
        final NullLocal local = new NullLocal("l");
        final Host bookmark = new Host(new TestProtocol());
        final Transfer t = new DownloadTransfer(bookmark, file, local);
        final NullSession session = new NullSession(bookmark);
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t),
                new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService());
        assertEquals(5L, worker.failed(file, local, new AccessDeniedException()).getLength());
    }

    @Test
    public void testFailedStatusPrepared() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        file.attributes().setSize(5L);
        final NullLocal local = new NullLocal("l");
        final Host bookmark = new Host(new TestProtocol());
        final Transfer t = new DownloadTransfer(bookmark, file, local);
        final NullSession session = new NullSession(bookmark);
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t),
                new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService());
        final TransferStatus prepared = new TransferStatus().setLength(7L).setComplete();
        worker.getStatus().put(new TransferItem(file, local), prepared);
        final TransferStatus status = worker.failed(file, local, new AccessDeniedException());
        // Status determined by filter is preferred over length of file
        assertEquals(7L, status.getLength());
        assertNotSame(prepared, status);
        assertFalse(status.isComplete());
        // Status in table is not modified
        assertTrue(prepared.isComplete());
    }

    @Test
    public void testErrorCallbackStatusWhenFilterPrepareFails() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        file.attributes().setSize(5L);
        final AtomicReference<TransferStatus> failed = new AtomicReference<>();
        this.runWithFailingFilter(file, new TransferPathFilterStub() {
            @Override
            public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener listener) throws BackgroundException {
                throw new AccessDeniedException();
            }
        }, failed);
        assertNotNull(failed.get());
        // Length of remote file instead of unknown length of root parent status
        assertEquals(5L, failed.get().getLength());
        assertFalse(failed.get().isComplete());
    }

    @Test
    public void testErrorCallbackStatusWhenFilterApplyFails() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        file.attributes().setSize(5L);
        final AtomicReference<TransferStatus> prepared = new AtomicReference<>();
        final AtomicReference<TransferStatus> failed = new AtomicReference<>();
        this.runWithFailingFilter(file, new TransferPathFilterStub() {
            @Override
            public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener listener) {
                final TransferStatus status = new TransferStatus().setLength(7L);
                prepared.set(status);
                return status;
            }

            @Override
            public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
                throw new AccessDeniedException();
            }
        }, failed);
        assertNotNull(failed.get());
        // Status determined by filter is passed to callback
        assertEquals(7L, failed.get().getLength());
        assertNotSame(prepared.get(), failed.get());
    }

    /**
     * Run copy transfer of single file with filter and error callback continuing on failure capturing status
     */
    private void runWithFailingFilter(final Path file, final TransferPathFilter filter, final AtomicReference<TransferStatus> failed) throws BackgroundException {
        final Host bookmark = new Host(new TestProtocol());
        final Transfer t = new CopyTransfer(bookmark, bookmark,
                Collections.singletonMap(file, new Path("/c", EnumSet.of(Path.Type.file)))) {
            @Override
            public TransferPathFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return filter;
            }
        };
        final NullSession session = new NullSession(bookmark);
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new TransferErrorCallback() {
            @Override
            public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) {
                assertEquals(file, item.remote);
                assertTrue(failed.compareAndSet(null, status));
                // Continue
                return true;
            }
        }, ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService()) {
            @Override
            public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) {
                // Skip transfer
                return null;
            }
        };
        worker.run(session);
    }

    private static class TransferPathFilterStub implements TransferPathFilter {
        @Override
        public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener listener) {
            return true;
        }

        @Override
        public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener listener) throws BackgroundException {
            return new TransferStatus();
        }

        @Override
        public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
            //
        }

        @Override
        public void complete(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
            //
        }
    }

    @Test(expected = NotfoundException.class)
    public void testUploadFileNotFound() throws Exception {
        // #7791
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        final Host bookmark = new Host(new TestProtocol());
        final Transfer t = new UploadTransfer(bookmark, root,
                new NullLocal("l") {
                    @Override
                    public boolean exists() {
                        // Will give a not found failure
                        return false;
                    }
                });
        final NullSession session = new NullSession(bookmark) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        try {
            new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
                @Override
                public TransferAction prompt(final TransferItem file) {
                    return TransferAction.overwrite;
                }
            }, new DisabledTransferErrorCallback(),
                    ProgressListener.noop, StreamListener.noop, LoginCallback.noop, new DisabledNotificationService()) {
                @Override
                public Future<TransferStatus> transfer(final TransferItem file, final TransferAction action) {
                    // Expected not found
                    fail();
                    return null;
                }
            }.run(session);
        }
        catch(NotfoundException e) {
            // Expected
            assertFalse(t.isComplete());
            throw e;
        }
    }
}
