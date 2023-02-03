package ch.cyberduck.core.worker;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Future;

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
                                 final TransferOptions options, final TransferStatus overall, final TransferStatus segment,
                                 final ConnectionCallback connectionCallback,
                                 final ProgressListener listener, final StreamListener streamListener) {
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
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {
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
                                 final TransferOptions options, final TransferStatus overall, final TransferStatus segment,
                                 final ConnectionCallback connectionCallback,
                                 final ProgressListener listener, final StreamListener streamListener) {
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
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {
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
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus overall, final TransferStatus segment,
                                 final ConnectionCallback connectionCallback,
                                 final ProgressListener listener, final StreamListener streamListener) {
                if(file.equals(root)) {
                    assertTrue(segment.isExists());
                }
                else {
                    assertFalse(segment.isExists());
                }
            }

            @Override
            public AbstractDownloadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return super.filter(source, destination, action, listener).withAttributes(new AttributesFinder() {
                    @Override
                    public PathAttributes find(final Path file, final ListProgressListener listener) {
                        return file.attributes();
                    }
                });
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<>();
                children.add(child);
                return children;
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {
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
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {
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
