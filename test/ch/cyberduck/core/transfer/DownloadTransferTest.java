package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.serializer.TransferDictionary;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.ui.action.SingleTransferWorker;

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
public class DownloadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
        t.addSize(4L);
        t.addTransferred(3L);
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
        assertFalse(serialized.isComplete());
    }

    @Test
    public void testSerializeComplete() throws Exception {
        // Test transfer to complete with existing directory
        Transfer t = new DownloadTransfer(new Host("t"), new Path("/t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        });
        new SingleTransferWorker(new NullSession(new Host("t")), t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledProgressListener(), new DisabledLoginController()).run();
        assertTrue(t.isComplete());
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertTrue(serialized.isComplete());
    }

    @Test
    public void testList() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("l"));
        final NullSession session = new NullSession(new Host("t")) {
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
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("l"));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/t/c", EnumSet.of(Path.Type.file)));
                children.add(new Path("/t/c.html", EnumSet.of(Path.Type.file)));
                return children;
            }
        };
        {
            Preferences.instance().setProperty("queue.download.priority.regex", ".*\\.html");
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
            Preferences.instance().deleteProperty("queue.download.priority.regex");
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
        Transfer t = new DownloadTransfer(new Host("t"), root, null);
        final NullSession session = new NullSession(new Host("t")) {
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
    public void testPrepareDownloadOverrideFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path test = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final Transfer transfer = new DownloadTransfer(new Host("t"), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledProgressListener(), new DisabledLoginController(), table);
        worker.prepare(test, new NullLocal(System.getProperty("java.io.tmpdir")), new TransferStatus().exists(true),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.singletonList(new TransferItem(test))),
                        new NullSession(new Host("h")))
        );
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        assertEquals(status, table.get(test));
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(false);
        expected.setLength(5L);
        expected.setCurrent(0L);
        assertEquals(expected, table.get(new Path("/transfer/test", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testPrepareDownloadResumeFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path test = new Path("/transfer/test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(5L);
        final Local local = new FinderLocal(System.getProperty("java.io.tmpdir") + "/transfer/test");
        LocalTouchFactory.get().touch(local);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write("test", out);
        IOUtils.closeQuietly(out);
        final Transfer transfer = new DownloadTransfer(host, test, local);
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledProgressListener(), new DisabledLoginController(), table);
        worker.prepare(test, local, new TransferStatus().exists(true),
                new ResumeFilter(new DownloadSymlinkResolver(Collections.singletonList(new TransferItem(test))),
                        new NullSession(new Host("h")))
        );
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        final TransferStatus expected = new TransferStatus();
        expected.setAppend(true);
        expected.setExists(true);
        expected.setCurrent("test".getBytes().length);
        // Remote size
        expected.setLength(5L);
        assertEquals(expected, table.get(test));
        local.delete();
    }

    @Test
    public void testActionFileExistsTrue() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
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
        assertEquals(TransferAction.callback, t.action(new NullSession(new Host("t")), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                prompt.set(true);
                return TransferAction.callback;
            }
        }));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionFileExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.overwrite, t.action(new NullSession(new Host("t")), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return TransferAction.callback;
            }
        }));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsTrue() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
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
        assertEquals(TransferAction.callback, t.action(new NullSession(new Host("t")), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                prompt.set(true);
                return TransferAction.callback;
            }
        }));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(TransferAction.overwrite, t.action(new NullSession(new Host("t")), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return TransferAction.callback;
            }
        }));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionResume() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertEquals(TransferAction.resume, t.action(new NullSession(new Host("t")), true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }));
    }

    @Test
    public void testStatus() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertFalse(t.isRunning());
        assertFalse(t.isReset());
        assertNull(t.getTimestamp());
    }

    @Test
    public void testRegexFilter() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new DownloadTransfer(new Host("t"), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        final NullSession session = new NullSession(new Host("t")) {
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
        assertTrue(list.contains(new TransferItem(new Path("/t/t", EnumSet.of(Path.Type.file)), new NullLocal(System.getProperty("java.io.tmpdir"), "t"))));
    }
}
