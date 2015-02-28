package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.test.NullLocal;
import ch.cyberduck.core.test.NullSession;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;
import ch.cyberduck.core.transfer.upload.ResumeFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class UploadTransferTest extends AbstractTestCase {

    @Test
    public void testChildrenEmpty() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory)) {
        };
        Transfer t = new UploadTransfer(new Host("t"), root,
                new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        assertTrue(t.list(new NullSession(new Host("t")), root, new NullLocal("t") {
            @Override
            public AttributedList<Local> list() {
                return AttributedList.emptyList();
            }
        }, new DisabledListProgressListener()).isEmpty());
    }

    @Test
    public void testList() throws Exception {
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        };
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host("t"), root, local);
        assertEquals(Collections.<TransferItem>singletonList(new TransferItem(new Path("/t/c", EnumSet.of(Path.Type.file)), new NullLocal("t/c"))),
                t.list(new NullSession(new Host("t")), root, local, new DisabledListProgressListener()));
    }

    @Test
    public void testListSorted() throws Exception {
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                l.add(new NullLocal(this.getAbsolute(), "c.html"));
                return l;
            }
        };
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host("t"), root, local);
        {
            PreferencesFactory.get().setProperty("queue.upload.priority.regex", ".*\\.html");
            final List<TransferItem> list = t.list(new NullSession(new Host("t")), root, local, new DisabledListProgressListener());
            assertEquals(new NullLocal(local.getAbsolute(), "c.html"), list.get(0).local);
            assertEquals(new NullLocal(local.getAbsolute(), "c"), list.get(1).local);
        }
        {
            PreferencesFactory.get().deleteProperty("queue.upload.priority.regex");
            final List<TransferItem> list = t.list(new NullSession(new Host("t")), root, local, new DisabledListProgressListener());
            assertEquals(new NullLocal(local.getAbsolute(), "c.html"), list.get(1).local);
            assertEquals(new NullLocal(local.getAbsolute(), "c"), list.get(0).local);
        }
    }

    @Test
    public void testCacheResume() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
                l.add(new NullLocal(this.getAbsolute(), "b") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
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
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(root.getParent())) {
                    c.incrementAndGet();
                }
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                assertEquals(true, options.resumeRequested);
            }
        };
        final TransferOptions options = new TransferOptions();
        options.resumeRequested = true;
        new SingleTransferWorker(session, t, options, new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback()).run();
        assertEquals(1, c.get());
    }

    @Test
    public void testCacheRename() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
                l.add(new NullLocal(this.getAbsolute(), "b") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
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
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                c.incrementAndGet();
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                //
            }
        };
        new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.rename;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback()).run();
    }

    @Test
    public void testPrepareUploadOverrideFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final String name = UUID.randomUUID().toString();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), "transfer");
        LocalTouchFactory.get().touch(local);
        LocalTouchFactory.get().touch(new Local(local, name));
        final Transfer transfer = new UploadTransfer(host, test, local);
        Map<Path, TransferStatus> table
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
        worker.prepare(test, new Local(System.getProperty("java.io.tmpdir"), "transfer"), new TransferStatus().exists(true),
                new OverwriteFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session));
        assertEquals(new TransferStatus().exists(true), table.get(test));
        final TransferStatus expected = new TransferStatus();
        assertEquals(expected, table.get(new Path("/transfer/" + name, EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testPrepareUploadResumeFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final String name = "test";
        final Local local = new Local(System.getProperty("java.io.tmpdir") + "/transfer", name);
        LocalTouchFactory.get().touch(local);
        final OutputStream out = local.getOutputStream(false);
        final byte[] bytes = RandomStringUtils.random(1000).getBytes();
        IOUtils.write(bytes, out);
        IOUtils.closeQuietly(out);
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "transfer") {
            @Override
            public AttributedList<Local> list() throws AccessDeniedException {
                return new AttributedList<Local>(Collections.<Local>singletonList(local));
            }
        };
        final Transfer transfer = new UploadTransfer(host, test, directory);
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
        worker.prepare(test, directory, new TransferStatus().exists(true),
                new ResumeFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session));
        assertEquals(new TransferStatus().exists(true), table.get(test));
        final TransferStatus expected = new TransferStatus().exists(true);
        expected.setAppend(true);
        // Remote size
        expected.setOffset(5L);
        // Local size
        expected.setLength(bytes.length);
        assertEquals(expected, table.get(new Path("/transfer/" + name, EnumSet.of(Path.Type.file))));
        local.delete();
    }

    @Test
    public void testUploadTemporaryName() throws Exception {
        final Path test = new Path("/f", EnumSet.of(Path.Type.file));
        final AtomicBoolean moved = new AtomicBoolean();
        final Host host = new Host("t");
        final Session session = new NullSession(host) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path f) throws BackgroundException {
                            return true;
                        }

                        @Override
                        public Find withCache(PathCache cache) {
                            return this;
                        }
                    };
                }
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path file, final Path renamed, boolean exists, final ProgressListener listener) throws BackgroundException {
                            assertEquals(test, renamed);
                            moved.set(true);
                        }

                        @Override
                        public boolean isSupported(final Path file) {
                            return true;
                        }
                    };
                }
                if(type.equals(ch.cyberduck.core.features.Attributes.class)) {
                    return (T) new ch.cyberduck.core.features.Attributes() {
                        @Override
                        public PathAttributes find(final Path file) throws BackgroundException {
                            return new PathAttributes();
                        }

                        @Override
                        public ch.cyberduck.core.features.Attributes withCache(PathCache cache) {
                            return this;
                        }
                    };
                }
                if(type.equals(Write.class)) {
                    return (T) new Write() {
                        @Override
                        public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                            fail();
                            return new Write.Append(0L);
                        }

                        @Override
                        public boolean temporary() {
                            return true;
                        }

                        @Override
                        public boolean pooled() {
                            return false;
                        }
                    };
                }
                return null;
            }
        };
        final AtomicBoolean set = new AtomicBoolean();
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final Transfer transfer = new UploadTransfer(host, test, local) {
            @Override
            public void transfer(final Session<?> session, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback, final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                assertEquals(table.get(test).getRename().remote, file);
                status.setComplete();
                set.set(true);
            }
        };
        final OverwriteFilter filter = new OverwriteFilter(
                new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session,
                new UploadFilterOptions().withTemporary(true));
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(),
                new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), table);
        worker.prepare(test, local, new TransferStatus().exists(true), filter);
        assertNotNull(table.get(test));
        assertNotNull(table.get(test).getRename());
        worker.transfer(new TransferItem(test, local), filter);
        assertTrue(set.get());
        assertTrue(moved.get());
    }
}