package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SingleTransferWorkerTest extends AbstractTestCase {

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
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        };
        final Cache<TransferItem> cache = new Cache<TransferItem>(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host("t"), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local, final TransferOptions options, final TransferStatus status, final ConnectionCallback callback, final ProgressListener listener) throws BackgroundException {
                //
            }
        };
        new SingleTransferWorker(new NullSession(new Host("t")), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(), new DisabledProgressListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferPathFilter filter) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                }), filter);
                assertFalse(cache.containsKey(child.getReference()));
            }
        }.run();
        assertFalse(cache.containsKey(child.getReference()));
    }

    @Test
    public void testUploadPrepareOverrideRootExists() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("l") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        };
        final Cache<TransferItem> cache = new Cache<TransferItem>(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host("t"), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local, final TransferOptions options, final TransferStatus status, final ConnectionCallback callback, final ProgressListener listener) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
                }
            }
        };
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.<Path>singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(), new DisabledProgressListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferPathFilter filter) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(item, filter);
                assertFalse(cache.containsKey(child.getReference()));
            }
        }.run();
        assertFalse(cache.containsKey(child.getReference()));
        assertTrue(cache.isEmpty());
    }

    @Test
    public void testDownloadPrepareOverride() throws Exception {
        final Path child = new Path("/t/c", EnumSet.of(Path.Type.file));
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final Cache<TransferItem> cache = new Cache<TransferItem>(Integer.MAX_VALUE);
        final Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("l")) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local, final TransferOptions options, final TransferStatus status, final ConnectionCallback callback, final ProgressListener listener) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
                }
            }
        };
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(child);
                return children;
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(), new DisabledProgressListener(), new DisabledLoginCallback(), cache) {
            @Override
            public void transfer(final TransferItem item, final TransferPathFilter filter) throws BackgroundException {
                if(item.remote.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(new TransferItem(item.remote, new NullLocal("l")), filter);
                if(item.remote.equals(root)) {
                    assertFalse(cache.containsKey(root.getReference()));
                }
            }
        };
        worker.run();
        assertFalse(cache.containsKey(child.getReference()));
        assertTrue(cache.isEmpty());
    }

    @Test(expected = NotfoundException.class)
    public void testUploadFileNotFound() throws Exception {
        // #7791
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host("t"), root,
                new NullLocal("l") {
                    @Override
                    public boolean exists() {
                        // Will give a not found failure
                        return false;
                    }
                });
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.<Path>singletonList(new Path("/t", EnumSet.of(Path.Type.directory))));
            }
        };
        try {
            new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
                @Override
                public TransferAction prompt() {
                    return TransferAction.overwrite;
                }
            }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(), new DisabledProgressListener(), new DisabledLoginCallback(), Cache.<TransferItem>empty()) {
                @Override
                public void transfer(final TransferItem file, final TransferPathFilter filter) throws BackgroundException {
                    // Expected not found
                    fail();
                }
            }.run();
        }
        catch(NotfoundException e) {
            // Expected
            assertFalse(t.isComplete());
            throw e;
        }
    }
}
