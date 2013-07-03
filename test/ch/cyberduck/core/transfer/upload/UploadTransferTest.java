package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
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
    public void testPrepareOverride() throws Exception {
        final NullPath child = new NullPath("/t/c", Path.FILE_TYPE);
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        });
        final Transfer t = new UploadTransfer(root) {
            @Override
            protected void prepare(final Path file, final TransferPathFilter filter, final TransferStatus status) throws BackgroundException {
                super.prepare(file, filter, status);
                if(file.equals(root)) {
                    assertTrue(status.isOverride());
                }
                else if(file.equals(child)) {
                    assertFalse(status.isOverride());
                }
                else {
                    fail();
                }
            }

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
                else if(file.equals(child)) {
                    assertFalse(status.isOverride());
                }
                else {
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
    }


    @Test
    public void testPrepareOverride2() throws Exception {
        final NullPath child = new NullPath("/t/c", Path.FILE_TYPE);
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            public boolean exists() {
                return false;
            }
        };
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                AttributedList<Local> l = new AttributedList<Local>();
                l.add(new NullLocal(this.getAbsolute(), "c"));
                return l;
            }
        });
        final Transfer t = new UploadTransfer(root) {
            @Override
            protected void prepare(final Path file, final TransferPathFilter filter, final TransferStatus status) throws BackgroundException {
                super.prepare(file, filter, status);
                if(file.equals(root)) {
                    assertFalse(status.isOverride());
                }
                else if(file.equals(child)) {
                    assertFalse(status.isOverride());
                }
                else {
                    fail();
                }
            }

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
                    assertFalse(status.isOverride());
                }
                else if(file.equals(child)) {
                    assertFalse(status.isOverride());
                }
                else {
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
    }

    @Test
    public void testChildren() throws Exception {
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Session getSession() {
                return new SFTPSession(new Host(Protocol.SFTP, "t"));
            }

            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public AttributedList<Local> list() {
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }

                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        };
        Transfer t = new UploadTransfer(root);
        assertEquals(Collections.<Path>singletonList(new NullPath("/t/c", Path.FILE_TYPE)), t.children(root));
    }
}