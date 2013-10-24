package ch.cyberduck.core.transfer;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;
import ch.cyberduck.core.transfer.upload.ResumeFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.ui.action.SingleTransferWorker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
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
    public void testSerialize() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        Transfer t = new UploadTransfer(new Host("t"), test);
        t.addSize(4L);
        t.addTransferred(3L);
        final UploadTransfer serialized = new UploadTransfer(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testChildrenEmpty() throws Exception {
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public AttributedList<Local> list() {
                        return AttributedList.emptyList();
                    }
                };
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root);
        assertTrue(t.list(new NullSession(new Host("t")), root, new DisabledListProgressListener()).isEmpty());
    }

    @Test
    public void testPrepareOverrideRootExists() throws Exception {
        final Path child = new Path("/t/c", Path.FILE_TYPE);
        final Path root = new Path("/t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        });
        final Cache cache = new Cache(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host("t"), root) {
            @Override
            public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
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
                return new AttributedList<Path>(Collections.<Path>singletonList(new Path("/t", Path.DIRECTORY_TYPE)));
            }
        };
        new SingleTransferWorker(session, t, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), cache) {
            @Override
            public void transfer(final Path file, final TransferPathFilter filter,
                                 final TransferOptions options, final TransferErrorCallback error) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(file, filter, options, new DisabledTransferErrorCallback());
                assertFalse(cache.containsKey(child.getReference()));
            }
        }.run();
        assertFalse(cache.containsKey(child.getReference()));
        assertTrue(cache.isEmpty());
    }


    @Test
    public void testPrepareOverrideRootDoesNotExist() throws Exception {
        final Path child = new Path("/t/c", Path.FILE_TYPE);
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Path getParent() {
                return new Path("/", Path.DIRECTORY_TYPE);
            }
        };
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        });
        final Cache cache = new Cache(Integer.MAX_VALUE);
        final Transfer t = new UploadTransfer(new Host("t"), root) {
            @Override
            public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                //
            }
        };
        new SingleTransferWorker(new NullSession(new Host("t")), t, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), cache) {
            @Override
            public void transfer(final Path file, final TransferPathFilter filter,
                                 final TransferOptions options, final TransferErrorCallback error) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(cache.containsKey(root.getReference()));
                }
                super.transfer(file, filter, options, new DisabledTransferErrorCallback());
                assertFalse(cache.containsKey(child.getReference()));
            }
        }.run();
        assertFalse(cache.containsKey(child.getReference()));
    }

    @Test
    public void testChildren() throws Exception {
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                };
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root);
        assertEquals(Collections.<Path>singletonList(new Path("/t/c", Path.FILE_TYPE)), t.list(new NullSession(new Host("t")), root,
                new DisabledListProgressListener()));
    }

    @Test
    public void testCacheResume() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "a"));
                        l.add(new NullLocal(this.getAbsolute(), "b"));
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                };
            }
        };
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(root.getParent())) {
                    c.incrementAndGet();
                }
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root) {
            @Override
            public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                assertEquals(true, options.resumeRequested);
            }
        };
        final TransferOptions options = new TransferOptions();
        options.resumeRequested = true;
        new SingleTransferWorker(session, t, options, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback()).run();
        assertEquals(1, c.get());
    }

    @Test
    public void testCacheRename() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "a"));
                        l.add(new NullLocal(this.getAbsolute(), "b"));
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }
                };
            }
        };
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                c.incrementAndGet();
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(new Host("t"), root) {
            @Override
            public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                //
            }
        };
        new SingleTransferWorker(session, t, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.rename;
            }
        }, new DisabledTransferErrorCallback()).run();
    }

    @Test
    public void testPrepareUploadOverrideFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/transfer", Path.DIRECTORY_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), "transfer"));
        final String name = UUID.randomUUID().toString();
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir") + "/transfer/" + name);
        local.touch();
        final Transfer transfer = new UploadTransfer(host, test);
        Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), table);
        worker.prepare(test, new TransferStatus().exists(true),
                new OverwriteFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session));

        assertEquals(new TransferStatus().exists(true), table.get(test));
        final TransferStatus expected = new TransferStatus();
        assertEquals(expected, table.get(new Path("/transfer/" + name, Path.FILE_TYPE)));
    }

    @Test
    public void testPrepareUploadResumeFilter() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/transfer", Path.DIRECTORY_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), "transfer"));
        final String name = "test";
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir") + "/transfer/" + name);
        local.touch();
        final OutputStream out = local.getOutputStream(false);
        final byte[] bytes = RandomStringUtils.random(1000).getBytes();
        IOUtils.write(bytes, out);
        IOUtils.closeQuietly(out);
        final Transfer transfer = new UploadTransfer(host, test);
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), table);
        worker.prepare(test, new TransferStatus().exists(true),
                new ResumeFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session));
        assertEquals(new TransferStatus().exists(true), table.get(test));
        final TransferStatus expected = new TransferStatus().exists(true);
        expected.setAppend(true);
        // Remote size
        expected.setCurrent(5L);
        // Local size
        expected.setLength(bytes.length);
        assertEquals(expected, table.get(new Path("/transfer/" + name, Path.FILE_TYPE)));
        local.delete();
    }

    @Test
    public void testUploadTemporaryName() throws Exception {
        final Path test = new Path(new Path("/", Path.DIRECTORY_TYPE),
                new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
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
                    };
                }
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path file, final Path renamed, boolean exists) throws BackgroundException {
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
                        public PathAttributes getAttributes(final Path file) throws BackgroundException {
                            return new PathAttributes(Path.FILE_TYPE);
                        }
                    };
                }
                return null;
            }
        };
        test.getLocal().touch();
        final AtomicBoolean set = new AtomicBoolean();
        final Map<Path, TransferStatus> table
                = new HashMap<Path, TransferStatus>();
        final Transfer transfer = new UploadTransfer(host, test) {
            @Override
            public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                assertEquals(table.get(test).getRenamed(), file);
                set.set(true);
            }
        };
        final OverwriteFilter filter = new OverwriteFilter(
                new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session,
                new UploadFilterOptions().withTemporary(true));
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions(), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                fail();
                return null;
            }
        }, new DisabledTransferErrorCallback(), table);
        worker.prepare(test, new TransferStatus().exists(true), filter);
        assertNotNull(table.get(test));
        assertNotNull(table.get(test).getRenamed());
        worker.transfer(test, filter, new TransferOptions(), new DisabledTransferErrorCallback());
        assertTrue(set.get());
        assertTrue(moved.get());
    }
}