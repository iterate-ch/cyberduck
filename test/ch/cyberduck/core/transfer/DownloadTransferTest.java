package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.ui.action.SingleTransferWorker;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DownloadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), test);
        t.addSize(4L);
        t.addTransferred(3L);
        final DownloadTransfer serialized = new DownloadTransfer(t.serialize(SerializerFactory.get()), new SFTPSession(new Host(new SFTPProtocol(), "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
        assertFalse(serialized.isComplete());
    }

    @Test
    public void testSerializeComplete() throws Exception {
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        });
        new SingleTransferWorker(t, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new DisabledTransferErrorCallback()).run();
        assertTrue(t.isComplete());
        final DownloadTransfer serialized = new DownloadTransfer(t.serialize(SerializerFactory.get()), new SFTPSession(new Host(new SFTPProtocol(), "t")));
        assertNotSame(t, serialized);
        assertTrue(serialized.isComplete());
    }

    @Test
    public void testChildren() throws Exception {
        final Path root = new Path("/t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(null, "l"));
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/t/c", Path.FILE_TYPE));
                return children;
            }
        }, root);
        assertEquals(Collections.<Path>singletonList(new Path("/t/c", Path.FILE_TYPE)), t.list(root, new TransferStatus().exists(true)));
    }

    @Test
    public void testChildrenEmpty() throws Exception {
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
        };
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        }, root);
        assertTrue(t.list(root, new TransferStatus().exists(true)).isEmpty());
    }

    @Test
    public void testPrepareOverride() throws Exception {
        final Path child = new Path("/t/c", Path.FILE_TYPE);
        final Path root = new Path("/t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public boolean exists() {
                return true;
            }
        });
        final Cache cache = new Cache(Integer.MAX_VALUE);
        final Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(child);
                return children;
            }
        }, root) {
            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
                }
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(t, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new DisabledTransferErrorCallback(), cache) {
            @Override
            public void transfer(final Path file, final TransferPathFilter filter,
                                 final TransferOptions options, final TransferErrorCallback error) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(file, filter, options, new DisabledTransferErrorCallback());
                if(file.equals(root)) {
                    assertFalse(cache.containsKey(root.getReference()));
                }
            }
        };
        worker.run();
        assertFalse(cache.containsKey(child.getReference()));
        assertTrue(cache.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testDownloadPath() throws Exception {
        final Path root = new Path("/t", Path.FILE_TYPE);
        assertNull(root.getLocal());
        List<Path> roots = new ArrayList<Path>();
        roots.add(root);
        roots.add(root);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), roots);
    }

    @Test
    public void testCustomDownloadPath() throws Exception {
        final Path root = new Path("/t", Path.FILE_TYPE);
        final NullLocal l = new NullLocal(null, "n");
        root.setLocal(l);
        assertNotNull(root.getLocal());
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), root);
        assertEquals(l, root.getLocal());
    }

    @Test
    public void testPrepareDownloadOverrideFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/transfer", Path.DIRECTORY_TYPE);
        test.setLocal(new NullLocal(UUID.randomUUID().toString(), "transfer"));
        final Transfer transfer = new DownloadTransfer(session, test);
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(transfer, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), table);
        worker.prepare(test, new TransferStatus().exists(true),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.singletonList(test)), new NullSession(new Host("h"))));
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        assertEquals(status, table.get(test));
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(false);
        expected.setLength(5L);
        expected.setCurrent(0L);
        assertEquals(expected, table.get(new Path("/transfer/test", Path.FILE_TYPE)));
    }

    @Test
    public void testPrepareDownloadResumeFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/transfer", Path.DIRECTORY_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), "transfer"));
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir") + "/transfer/test");
        local.touch();
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write("test", out);
        IOUtils.closeQuietly(out);
        final Transfer transfer = new DownloadTransfer(session, test);
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(transfer, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), table);
        worker.prepare(test, new TransferStatus().exists(true),
                new ResumeFilter(new DownloadSymlinkResolver(Collections.singletonList(test)), new NullSession(new Host("h"))));
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        assertEquals(status, table.get(test));
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(true);
        expected.setCurrent("test".getBytes().length);
        // Remote size
        expected.setLength(5L);
        assertEquals(expected, table.get(new Path("/transfer/test", Path.FILE_TYPE)));
        local.delete();
    }

    @Test
    public void testActionFileExists() throws Exception {
        final Path root = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), root);
        root.setLocal(new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.ACTION_CALLBACK, t.action(false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                prompt.set(true);
                return TransferAction.ACTION_CALLBACK;
            }
        }));
        assertTrue(prompt.get());
        root.setLocal(new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }
        });
        assertEquals(TransferAction.ACTION_OVERWRITE, t.action(false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.ACTION_CALLBACK;
            }
        }));
    }

    @Test
    public void testActionDirectoryExists() throws Exception {
        final Path root = new Path("t", Path.DIRECTORY_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), root);
        root.setLocal(new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.ACTION_CALLBACK, t.action(false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                prompt.set(true);
                return TransferAction.ACTION_CALLBACK;
            }
        }));
        assertTrue(prompt.get());
        root.setLocal(new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }
        });
        assertEquals(TransferAction.ACTION_OVERWRITE, t.action(false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return TransferAction.ACTION_CALLBACK;
            }
        }));
    }

    @Test
    public void testActionResume() throws Exception {
        final Path root = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), root);
        assertEquals(TransferAction.ACTION_RESUME, t.action(true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }));
    }

    @Test
    public void testStatus() throws Exception {
        final Path parent = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), parent);
        assertFalse(t.isRunning());
        assertFalse(t.isCanceled());
        assertFalse(t.isReset());
        assertNull(t.getTimestamp());
    }

    @Test
    public void testRegexFilter() throws Exception {
        final Path parent = new Path("t", Path.DIRECTORY_TYPE);
        parent.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir")));
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<Path>();
                l.add(new Path("/t/.DS_Store", Path.FILE_TYPE));
                l.add(new Path("/t/t", Path.FILE_TYPE));
                return l;

            }
        }, parent);
        final AttributedList<Path> list = t.list(parent, new TransferStatus().exists(true));
        assertEquals(1, list.size());
        assertFalse(list.contains(new Path("/t/.DS_Store", Path.FILE_TYPE)));
        assertTrue(list.contains(new Path("/t/t", Path.FILE_TYPE)));
    }
}
