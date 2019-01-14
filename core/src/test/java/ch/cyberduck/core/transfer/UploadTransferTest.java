package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.filter.UploadRegexFilter;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.core.transfer.upload.UploadRegexPriorityComparator;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class UploadTransferTest {

    @Test
    public void testChildrenEmpty() throws Exception {
        final Path root = new Path("/t", EnumSet.of(Path.Type.directory)) {
        };
        Transfer t = new UploadTransfer(new Host(new TestProtocol()), root,
            new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        assertTrue(t.list(new NullSession(new Host(new TestProtocol())), root, new NullLocal("t") {
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
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        };
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local);
        assertEquals(Collections.singletonList(new TransferItem(new Path("/t/c", EnumSet.of(Path.Type.file)), new NullLocal("t", "c"))),
            t.list(new NullSession(new Host(new TestProtocol())), root, local, new DisabledListProgressListener()));
    }

    @Test
    public void testListSorted() throws Exception {
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                l.add(new NullLocal(this.getAbsolute(), "c.html"));
                return l;
            }
        };
        final Path root = new Path("/t", EnumSet.of(Path.Type.file));
        {
            Transfer t = new UploadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(root, local)),
                new UploadRegexFilter(), new UploadRegexPriorityComparator(".*\\.html"));
            final List<TransferItem> list = t.list(new NullSession(new Host(new TestProtocol())), root, local, new DisabledListProgressListener());
            assertEquals(new NullLocal(local.getAbsolute(), "c.html"), list.get(0).local);
            assertEquals(new NullLocal(local.getAbsolute(), "c"), list.get(1).local);
        }
        {
            Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local, new UploadRegexFilter());
            final List<TransferItem> list = t.list(new NullSession(new Host(new TestProtocol())), root, local, new DisabledListProgressListener());
            assertEquals(new NullLocal(local.getAbsolute(), "c.html"), list.get(1).local);
            assertEquals(new NullLocal(local.getAbsolute(), "c"), list.get(0).local);
        }
    }

    @Test
    public void testCacheResume() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
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
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws ConnectionCanceledException {
                if(folder.equals(root.getParent())) {
                    c.incrementAndGet();
                }
                listener.chunk(folder, AttributedList.emptyList());
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public Path transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback connectionCallback,
                                 final PasswordCallback passwordCallback, final ProgressListener listener, final StreamListener streamListener) {
                assertTrue(options.resumeRequested);
                return file;
            }
        };
        final TransferOptions options = new TransferOptions();
        options.resumeRequested = true;
        new SingleTransferWorker(session, null, t, options, new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService()).run();
        assertEquals(1, c.get());
    }

    @Test
    public void testCacheRename() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullLocal local = new NullLocal("t") {
            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
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
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws ConnectionCanceledException {
                c.incrementAndGet();
                listener.chunk(folder, AttributedList.emptyList());
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host(new TestProtocol()), root, local) {
            @Override
            public Path transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback connectionCallback,
                                 final PasswordCallback passwordCallback, final ProgressListener listener, final StreamListener streamListener) {
                //
                return file;
            }
        };
        new SingleTransferWorker(session, null, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.rename;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService()).run();
        assertEquals(1, c.get());
    }

    @Test
    public void testPrepareUploadOverrideFilter() throws Exception {
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
        final String name = UUID.randomUUID().toString();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), "transfer");
        LocalTouchFactory.get().touch(local);
        LocalTouchFactory.get().touch(new Local(local, name));
        final Transfer transfer = new UploadTransfer(host, test, local);
        final SingleTransferWorker worker = new SingleTransferWorker(session, null, transfer, new TransferOptions(),
            new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService());
        worker.prepare(test, new Local(System.getProperty("java.io.tmpdir"), "transfer"), new TransferStatus().exists(true),
            TransferAction.overwrite);
        assertEquals(new TransferStatus().exists(true), worker.getStatus().get(new TransferItem(test, local)));
        final TransferStatus expected = new TransferStatus();
        assertEquals(expected, worker.getStatus().get(new TransferItem(new Path("/transfer/" + name, EnumSet.of(Path.Type.file)), new Local(local, name))));
    }

    @Test
    public void testPrepareUploadResumeFilter() throws Exception {
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
        final Path testDirectory = new Path("/transfer", EnumSet.of(Path.Type.directory));
        final String name = "test";
        final Local localDirectory = new Local(System.getProperty("java.io.tmpdir"), "transfer") {
            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Collections.singletonList(new Local(this, name)));
            }
        };
        LocalTouchFactory.get().touch(new Local(localDirectory, name));
        final OutputStream out = new Local(localDirectory, name).getOutputStream(false);
        final byte[] bytes = new RandomStringGenerator.Builder().build().generate(1000).getBytes();
        IOUtils.write(bytes, out);
        out.close();
        final Transfer transfer = new UploadTransfer(host, testDirectory, localDirectory);
        final SingleTransferWorker worker = new SingleTransferWorker(session, null, transfer, new TransferOptions(),
            new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService());
        worker.prepare(testDirectory, localDirectory, new TransferStatus().exists(true),
            TransferAction.resume);
        assertEquals(new TransferStatus().exists(true), worker.getStatus().get(new TransferItem(testDirectory, localDirectory)));
        final TransferStatus expected = new TransferStatus().exists(true);
        expected.setAppend(true);
        // Remote size
        expected.setOffset(5L);
        // Local size
        expected.setLength(bytes.length - 5L);
        assertEquals(expected, worker.getStatus().get(new TransferItem(new Path("/transfer/" + name, EnumSet.of(Path.Type.file)), new Local(localDirectory, name))));
        new Local(localDirectory, name).delete();
    }

    @Test
    public void testUploadTemporaryName() throws Exception {
        final Path test = new Path("/f", EnumSet.of(Path.Type.file));
        final AtomicBoolean moved = new AtomicBoolean();
        final Host host = new Host(new TestProtocol());
        final Session session = new NullSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path f) {
                            return true;
                        }
                    };
                }
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) {
                            assertEquals(test, renamed);
                            moved.set(true);
                            return renamed;
                        }

                        @Override
                        public boolean isRecursive(final Path source, final Path target) {
                            return true;
                        }

                        @Override
                        public boolean isSupported(final Path source, final Path target) {
                            return true;
                        }

                        @Override
                        public Move withDelete(final Delete delete) {
                            return this;
                        }

                    };
                }
                if(type.equals(AttributesFinder.class)) {
                    return (T) new AttributesFinder() {
                        @Override
                        public PathAttributes find(final Path file) {
                            return new PathAttributes();
                        }
                    };
                }
                if(type.equals(Write.class)) {
                    return (T) new Write() {
                        @Override
                        public StatusOutputStream write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final Long length, final Cache cache) {
                            fail();
                            return new Write.Append(0L);
                        }


                        @Override
                        public boolean temporary() {
                            return true;
                        }

                        @Override
                        public boolean random() {
                            return false;
                        }
                    };
                }
                return (T) super._getFeature(type);
            }
        };
        final AtomicBoolean set = new AtomicBoolean();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final Transfer transfer = new UploadTransfer(host, test, local) {
            @Override
            public Path transfer(final Session<?> source, final Session<?> destination, final Path file, Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback connectionCallback, final PasswordCallback passwordCallback, final ProgressListener listener, final StreamListener streamListener) {
                status.setComplete();
                set.set(true);
                return file;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return super.filter(source, destination, action, listener).withOptions(new UploadFilterOptions().withTemporary(true));
            }
        };
        final SingleTransferWorker worker = new SingleTransferWorker(session, null, transfer, new TransferOptions(),
            new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService()) {
            @Override
            public Future<TransferStatus> transfer(final TransferItem item, final TransferAction action) throws BackgroundException {
                return super.transfer(item, action);
            }
        };
        worker.prepare(test, local, new TransferStatus().exists(true), TransferAction.overwrite);
        assertNotNull(worker.getStatus().get(new TransferItem(test, local)));
        assertNotNull(worker.getStatus().get(new TransferItem(test, local)).getRename());
        worker.transfer(new TransferItem(test, local), TransferAction.overwrite);
        assertTrue(set.get());
        assertTrue(moved.get());
    }

    @Test
    public void testTemporaryDisabledLargeUpload() throws Exception {
        final Host h = new Host(new TestProtocol());
        final NullSession session = new NullSession(h);
        final AbstractUploadFilter f = new UploadTransfer(h, Collections.emptyList())
            .filter(session, null, TransferAction.overwrite, new DisabledProgressListener());
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus(), new DisabledProgressListener());
        assertNull(status.getRename().local);
        assertNull(status.getRename().remote);
    }

    @Test
    public void testTemporaryDisabledMultipartUpload() throws Exception {
        final Host h = new Host(new TestProtocol());
        final NullSession session = new NullSession(h);
        final AbstractUploadFilter f = new UploadTransfer(h, Collections.emptyList())
            .filter(session, null, TransferAction.overwrite, new DisabledProgressListener());
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus(), new DisabledProgressListener());
        assertNull(status.getRename().local);
        assertNull(status.getRename().remote);
    }
}
