package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class UploadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new UploadTransfer(new NullPath("t", Path.FILE_TYPE));
        t.addSize(4L);
        t.addTransferred(3L);
        final UploadTransfer serialized = new UploadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testChildrenEmpty() throws Exception {
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
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
        Transfer t = new UploadTransfer(root);
        assertTrue(t.children(root).isEmpty());
    }

    @Test
    public void testPrepareOverrideRootExists() throws Exception {
        final NullPath child = new NullPath("/t/c", Path.FILE_TYPE);
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Path getParent() {
                return new NullPath("/", Path.DIRECTORY_TYPE) {
                    @Override
                    public AttributedList<Path> list() {
                        return new AttributedList<Path>(Collections.<Path>singletonList(new NullPath("/t", Path.DIRECTORY_TYPE)));
                    }
                };
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
        final Transfer t = new UploadTransfer(root) {
            @Override
            protected void transfer(final Path file, final TransferPathFilter filter,
                                    final TransferOptions options, final TransferStatus status) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(this.cache().containsKey(root.getReference()));
                }
                super.transfer(file, filter, options, status);
                assertFalse(this.cache().containsKey(child.getReference()));
            }

            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                if(file.equals(root)) {
                    fail();
                }
            }
        };
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new TransferOptions());
        assertFalse(t.cache().containsKey(child.getReference()));
        assertTrue(t.cache().isEmpty());
    }


    @Test
    public void testPrepareOverrideRootDoesNotExist() throws Exception {
        final NullPath child = new NullPath("/t/c", Path.FILE_TYPE);
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Path getParent() {
                return new NullPath("/", Path.DIRECTORY_TYPE) {
                    @Override
                    public AttributedList<Path> list() {
                        return AttributedList.emptyList();
                    }
                };
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
        final Transfer t = new UploadTransfer(root) {
            @Override
            protected void transfer(final Path file, final TransferPathFilter filter,
                                    final TransferOptions options, final TransferStatus status) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(this.cache().containsKey(root.getReference()));
                }
                super.transfer(file, filter, options, status);
                assertFalse(this.cache().containsKey(child.getReference()));
            }

            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                //
            }
        };
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new TransferOptions());
        assertFalse(t.cache().containsKey(child.getReference()));
    }

    @Test
    public void testChildren() throws Exception {
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
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
        Transfer t = new UploadTransfer(root);
        assertEquals(Collections.<Path>singletonList(new NullPath("/t/c", Path.FILE_TYPE)), t.children(root));
    }

    @Test
    public void testCacheResume() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
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

            @Override
            public AttributedList<Path> list() {
                c.incrementAndGet();
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(root) {
            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                //
            }
        };
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_RESUME;
            }
        }, new TransferOptions());
        assertEquals(1, c.get());
    }

    @Test
    public void testCacheRename() throws Exception {
        final AtomicInteger c = new AtomicInteger();
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
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

            @Override
            public AttributedList<Path> list() {
                c.incrementAndGet();
                return AttributedList.emptyList();
            }
        };
        Transfer t = new UploadTransfer(root) {
            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
                //
            }
        };
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_RENAME;
            }
        }, new TransferOptions());
    }
}