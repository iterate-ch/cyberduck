package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.filter.DownloadRegexFilter;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
import ch.cyberduck.core.transfer.download.DownloadRegexPriorityComparator;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DownloadTransferTest {

    @Test
    public void testList() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal("l"));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/t/c", EnumSet.of(Path.Type.file)));
                return children;
            }
        };
        assertEquals(Collections.<TransferItem>singletonList(new TransferItem(new Path("/t/c", EnumSet.of(Path.Type.file)), new NullLocal("t/c"))),
                t.list(session, root, new NullLocal("t") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                }, new DisabledListProgressListener())
        );
    }

    @Test
    public void testListSorted() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/t/c", EnumSet.of(Path.Type.file)));
                children.add(new Path("/t/c.html", EnumSet.of(Path.Type.file)));
                return children;
            }
        };
        {
            Transfer t = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(root, new NullLocal("l"))), new DownloadRegexFilter(),
                    new DownloadRegexPriorityComparator(".*\\.html"));
            final List<TransferItem> list = t.list(session, root, new NullLocal("t") {
                @Override
                public boolean exists() {
                    return true;
                }
            }, new DisabledListProgressListener());
            assertEquals(new Path("/t/c.html", EnumSet.of(Path.Type.file)), list.get(0).remote);
            assertEquals(new Path("/t/c", EnumSet.of(Path.Type.file)), list.get(1).remote);
        }
        {
            Transfer t = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(root, new NullLocal("l"))), new DownloadRegexFilter(),
                    new DownloadRegexPriorityComparator());
            final List<TransferItem> list = t.list(session, root, new NullLocal("t") {
                @Override
                public boolean exists() {
                    return true;
                }
            }, new DisabledListProgressListener());
            assertEquals(new Path("/t/c.html", EnumSet.of(Path.Type.file)), list.get(1).remote);
            assertEquals(new Path("/t/c", EnumSet.of(Path.Type.file)), list.get(0).remote);
        }
    }

    @Test
    public void testChildrenEmpty() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, null);
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        };
        assertTrue(t.list(session, root, new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new DisabledListProgressListener()).isEmpty());
    }

    @Test
    public void testPrepareDownloadHttp() throws Exception {
        final Host host = new Host(new TestProtocol(), "update.cyberduck.io", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final Session<?> session = new NullSession(host);
        final Path test = new Path("/Cyberduck-4.6.zip", EnumSet.of(Path.Type.file));
        final Transfer transfer = new DownloadTransfer(new Host(new TestProtocol()), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback());
        worker.prepare(test, new NullLocal(System.getProperty("java.io.tmpdir"), "c"), new TransferStatus().exists(true),
                TransferAction.overwrite
        );
    }

    @Test
    public void testPrepareDownloadOverrideFilter() throws Exception {
        final Host host = new Host(new TestProtocol());
        final Session<?> session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory)))) {
                    return new AttributedList<>(Collections.singletonList(new Path("/transfer", EnumSet.of(Path.Type.directory))));
                }
                final Path f = new Path("/transfer/test", EnumSet.of(Path.Type.file));
                f.attributes().setSize(5L);
                return new AttributedList<>(Collections.singletonList(f));
            }
        };
        final Path test = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final Transfer transfer = new DownloadTransfer(new Host(new TestProtocol()), test, new NullLocal(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), TransferItemCache.empty(), table);
        worker.prepare(test, new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()), new TransferStatus().exists(true),
                TransferAction.overwrite
        );
        final TransferStatus status = new TransferStatus();
        status.setExists(false);
        assertEquals(status, table.get(test));
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(false);
        expected.setLength(5L);
        expected.setOffset(0L);
        expected.setExists(false);
        assertEquals(expected, table.get(new Path("/transfer/test", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testPrepareDownloadResumeFilter() throws Exception {
        final Host host = new Host(new TestProtocol());
        final Session<?> session = new NullSession(host);
        final Path test = new Path("/transfer/test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(5L);
        final Local local = new Local(System.getProperty("java.io.tmpdir") + "/transfer/" + UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write("test", out);
        out.close();
        final Transfer transfer = new DownloadTransfer(host, test, local) {
            @Override
            public AbstractDownloadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
                return new ResumeFilter(new DownloadSymlinkResolver(Collections.singletonList(new TransferItem(test))),
                        new NullSession(new Host(new TestProtocol())), new DownloadFilterOptions(), new DefaultDownloadFeature(session) {
                    @Override
                    public boolean offset(final Path file) throws BackgroundException {
                        return true;
                    }
                });
            }
        };
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), TransferItemCache.empty(), table);
        worker.prepare(test, local, new TransferStatus().exists(true), TransferAction.resume);
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(true);
        expected.setExists(true);
        expected.setOffset("test".getBytes().length);
        // Transfer length
        expected.setLength(5L - "test".getBytes().length);
        assertEquals(expected, table.get(test));
        local.delete();
    }

    @Test
    public void testActionFileExistsTrue() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.callback, t.action(new NullSession(new Host(new TestProtocol())), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                prompt.set(true);
                return TransferAction.callback;
            }
        }, new DisabledListProgressListener()));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionFileExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.overwrite, t.action(new NullSession(new Host(new TestProtocol())), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return TransferAction.callback;
            }
        }, new DisabledListProgressListener()));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsTrue() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.callback, t.action(new NullSession(new Host(new TestProtocol())), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                prompt.set(true);
                return TransferAction.callback;
            }
        }, new DisabledListProgressListener()));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.overwrite, t.action(new NullSession(new Host(new TestProtocol())), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return TransferAction.callback;
            }
        }, new DisabledListProgressListener()));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionResume() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), root, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertEquals(TransferAction.resume, t.action(new NullSession(new Host(new TestProtocol())), true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledListProgressListener()));
    }

    @Test
    public void testStatus() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertFalse(t.isRunning());
        assertFalse(t.isReset());
        assertNull(t.getTimestamp());
    }

    @Test
    public void testRegexFilter() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<Path>();
                l.add(new Path("/t/.DS_Store", EnumSet.of(Path.Type.file)));
                l.add(new Path("/t/t", EnumSet.of(Path.Type.file)));
                return l;
            }
        };
        final List<TransferItem> list = t.list(session, parent,
                new NullLocal(System.getProperty("java.io.tmpdir")), new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertFalse(list.contains(new TransferItem(new Path("/t/.DS_Store", EnumSet.of(Path.Type.file)))));
        assertTrue(list.contains(new TransferItem(new Path("/t/t", EnumSet.of(Path.Type.file)), new Local(System.getProperty("java.io.tmpdir"), "t"))));
    }

    @Test
    public void testDownloadDuplicateNameFolderAndFile() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<Path>();
                // File first in list
                l.add(new Path("/f", EnumSet.of(Path.Type.file)));
                l.add(new Path("/f", EnumSet.of(Path.Type.directory)));
                return l;
            }
        };
        final List<TransferItem> list = t.list(session, parent,
                new NullLocal(System.getProperty("java.io.tmpdir")), new DisabledListProgressListener());
        assertEquals(2, list.size());
        // Make sure folder is first in list
        assertTrue(list.get(0).equals(new TransferItem(new Path("/f", EnumSet.of(Path.Type.directory)), new Local(System.getProperty("java.io.tmpdir"), "f"))));
        assertTrue(list.contains(new TransferItem(new Path("/f", EnumSet.of(Path.Type.file)), new Local(System.getProperty("java.io.tmpdir"), "f"))));
    }
}
