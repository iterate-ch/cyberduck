package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class UploadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        Transfer t = new UploadTransfer(new NullSession(new Host("t")), test);
        TransferStatus saved = new TransferStatus();
        saved.setLength(4L);
        saved.setCurrent(3L);
        t.save(test, saved);
        final UploadTransfer serialized = new UploadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
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
        Transfer t = new UploadTransfer(new NullSession(new Host("t")), root);
        assertTrue(t.children(root).isEmpty());
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
        final Transfer t = new UploadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.<Path>singletonList(new Path("/t", Path.DIRECTORY_TYPE)));
            }
        }, root) {
            @Override
            protected void transfer(final Path file, final TransferPathFilter filter,
                                    final TransferOptions options) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(this.cache().containsKey(root.getReference()));
                }
                super.transfer(file, filter, options);
                assertFalse(this.cache().containsKey(child.getReference()));
            }

            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(status.isExists());
                }
                else {
                    assertFalse(status.isExists());
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
        final Transfer t = new UploadTransfer(new NullSession(new Host("t")), root) {
            @Override
            protected void transfer(final Path file, final TransferPathFilter filter,
                                    final TransferOptions options) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(this.cache().containsKey(root.getReference()));
                }
                super.transfer(file, filter, options);
                assertFalse(this.cache().containsKey(child.getReference()));
            }

            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
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
        Transfer t = new UploadTransfer(new NullSession(new Host("t")), root);
        assertEquals(Collections.<Path>singletonList(new Path("/t/c", Path.FILE_TYPE)), t.children(root));
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
        Transfer t = new UploadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(root.getParent())) {
                    c.incrementAndGet();
                }
                return AttributedList.emptyList();
            }
        }, root) {
            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status,
                                 final ProgressListener listener) throws BackgroundException {
                assertEquals(true, options.resumeRequested);
            }
        };
        final TransferOptions options = new TransferOptions();
        options.resumeRequested = true;
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                fail();
                return null;
            }
        }, options);
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
        Transfer t = new UploadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                c.incrementAndGet();
                return AttributedList.emptyList();
            }
        }, root) {
            @Override
            public void transfer(final Path file, final TransferOptions options, final TransferStatus status,
                                 final ProgressListener listener) throws BackgroundException {
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

    @Test
    public void testPrepareUploadOverrideFilter() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
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
        final Transfer transfer = new UploadTransfer(session, test);
        transfer.prepare(test, new TransferStatus().exists(true), new OverwriteFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()))
        );
        final TransferStatus directory = new TransferStatus();
        directory.setExists(true);
        assertEquals(directory, transfer.getStatus(test));
        final TransferStatus expected = new TransferStatus();
        assertEquals(expected, transfer.getStatus(new Path("/transfer/" + name, Path.FILE_TYPE)));
    }

    @Test
    public void testPrepareUploadResumeFilter() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
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
        IOUtils.write("te", out);
        IOUtils.closeQuietly(out);
        final Transfer transfer = new UploadTransfer(session, test);
        transfer.prepare(test, new TransferStatus().exists(true), new ResumeFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()))
        );
        final TransferStatus directorystatus = new TransferStatus();
        directorystatus.setExists(true);
        assertEquals(directorystatus, transfer.getStatus(test));
        final TransferStatus expected = new TransferStatus();
        expected.setResume(true);
        // Remote size
        expected.setCurrent(5L);
        // Local size
        expected.setLength(2L);
        assertEquals(expected, transfer.getStatus(new Path("/transfer/" + name, Path.FILE_TYPE)));
        local.delete();
    }
}