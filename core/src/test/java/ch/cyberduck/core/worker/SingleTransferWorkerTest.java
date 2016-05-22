package ch.cyberduck.core.worker;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
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
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                AttributedList<Local> l = new AttributedList<Local>();
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
        final Cache<TransferItem> cache = new TransferItemCache(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                //
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(new TransferItem(root, local)));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l") {
                    @Override
                    public AttributedList<Local> list() throws LocalAccessDeniedException {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                }), action);
                assertFalse(cache.containsKey(new TransferItem(child, local)));
            }
        }.run(session);
        assertFalse(cache.containsKey(new TransferItem(child, local)));
    }

    @Test
    public void testUploadPrepareOverrideRootExists() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("l") {
            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                AttributedList<Local> l = new AttributedList<Local>();
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
        final Cache<TransferItem> cache = new TransferItemCache(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
                }
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(new TransferItem(root, local)));
                }
                super.transfer(item, action);
                assertFalse(cache.containsKey(new TransferItem(child, local)));
            }
        }.run(session);
        assertFalse(cache.containsKey(new TransferItem(child, local)));
        assertTrue(cache.isEmpty());
    }

    @Test
    public void testDownloadPrepareOverride() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final Cache<TransferItem> cache = new TransferItemCache(Integer.MAX_VALUE);
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
            public AttributedList<Local> list() throws AccessDeniedException {
                return AttributedList.emptyList();
            }
        };
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
                }
            }

            @Override
            public AbstractDownloadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
                return super.filter(session, action, listener).withAttributes(new Attributes() {
                    @Override
                    public PathAttributes find(final Path file) throws BackgroundException {
                        return file.attributes();
                    }

                    @Override
                    public Attributes withCache(final PathCache cache) {
                        return this;
                    }
                });
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(child);
                return children;
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(new TransferItem(root, local)));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l")), action);
                if(item.remote.equals(root)) {
                    assertFalse(cache.containsKey(new TransferItem(root, local)));
                }
            }
        };
        worker.run(session);
        assertFalse(cache.containsKey(new TransferItem(child, local)));
        assertTrue(cache.isEmpty());
    }

    @Test(expected = NotfoundException.class)
    public void testUploadFileNotFound() throws Exception {
        // #7791
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host(new TestProtocol()), root,
                new NullLocal("l") {
                    @Override
                    public boolean exists() {
                        // Will give a not found failure
                        return false;
                    }
                });
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        try {
            new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
                @Override
                public TransferAction prompt(final TransferItem file) {
                    return TransferAction.overwrite;
                }
            }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                    new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), TransferItemCache.empty()) {
                @Override
                public void transfer(final TransferItem file, final TransferAction action) throws BackgroundException {
                    // Expected not found
                    fail();
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
