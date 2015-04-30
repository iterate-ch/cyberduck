package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.TransferDictionary;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.test.NullLocal;
import ch.cyberduck.core.test.NullSession;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
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
public class DownloadTransferTest extends AbstractTestCase {

    @Test
    public void testSerializeComplete() throws Exception {
        // Test transfer to complete with existing directory
        final Transfer t = new DownloadTransfer(new Host("t"), new Path("/t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
                return AttributedList.emptyList();
            }

            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return true;
            }
        });
        new SingleTransferWorker(new NullSession(new Host("t")), t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback()).run();
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
            Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("l"));
            PreferencesFactory.get().setProperty("queue.download.priority.regex", ".*\\.html");
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
            Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("l"));
            PreferencesFactory.get().deleteProperty("queue.download.priority.regex");
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
        final Transfer t = new DownloadTransfer(new Host("t"), root, null);
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
    public void testPrepareDownloadHttp() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "update.cyberduck.io", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        service.connect(session, PathCache.empty());
        final Path test = new Path("/Cyberduck-4.6.zip", EnumSet.of(Path.Type.file));
        final Transfer transfer = new DownloadTransfer(new Host("t"), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback());
        worker.prepare(test, new NullLocal(System.getProperty("java.io.tmpdir"), "c"), new TransferStatus().exists(true),
                TransferAction.overwrite
        );
    }

    @Test
    public void testPrepareDownloadOverrideFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final Transfer transfer = new DownloadTransfer(new Host("t"), test, new NullLocal(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
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
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), table);
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
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/transfer/test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(5L);
        final Local local = new Local(System.getProperty("java.io.tmpdir") + "/transfer/" + UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write("test", out);
        IOUtils.closeQuietly(out);
        final Transfer transfer = new DownloadTransfer(host, test, local) {
            @Override
            public AbstractDownloadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
                return new ResumeFilter(new DownloadSymlinkResolver(Collections.singletonList(new TransferItem(test))),
                        new NullSession(new Host("h")), new DownloadFilterOptions(), new DefaultDownloadFeature(session) {
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
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), table);
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
            public TransferAction prompt(final TransferItem file) {
                prompt.set(true);
                return TransferAction.callback;
            }
        }));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionFileExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
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
            public TransferAction prompt(final TransferItem file) {
                fail();
                return TransferAction.callback;
            }
        }));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsTrue() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
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
            public TransferAction prompt(final TransferItem file) {
                prompt.set(true);
                return TransferAction.callback;
            }
        }));
        assertTrue(prompt.get());
    }

    @Test
    public void testActionDirectoryExistsFalse() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal("p", "t") {
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
            public TransferAction prompt(final TransferItem file) {
                fail();
                return TransferAction.callback;
            }
        }));
        assertFalse(prompt.get());
    }

    @Test
    public void testActionResume() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host("t"), root, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertEquals(TransferAction.resume, t.action(new NullSession(new Host("t")), true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }));
    }

    @Test
    public void testStatus() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.file));
        final Transfer t = new DownloadTransfer(new Host("t"), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
        assertFalse(t.isRunning());
        assertFalse(t.isReset());
        assertNull(t.getTimestamp());
    }

    @Test
    public void testRegexFilter() throws Exception {
        final Path parent = new Path("t", EnumSet.of(Path.Type.directory));
        final Transfer t = new DownloadTransfer(new Host("t"), parent, new NullLocal(System.getProperty("java.io.tmpdir")));
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
