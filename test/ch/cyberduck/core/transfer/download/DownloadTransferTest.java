package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DownloadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), test);
        TransferStatus saved = new TransferStatus();
        saved.setLength(4L);
        saved.setCurrent(3L);
        t.save(test, saved);
        final DownloadTransfer serialized = new DownloadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
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
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new TransferOptions());
        assertTrue(t.isComplete());
        final DownloadTransfer serialized = new DownloadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertTrue(serialized.isComplete());
    }

    @Test
    public void testChildren() throws Exception {
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
        };
        root.setLocal(new NullLocal(null, "l"));
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/t/c", Path.FILE_TYPE));
                return children;
            }
        }, root);
        assertEquals(Collections.<Path>singletonList(new Path("/t/c", Path.FILE_TYPE)), t.children(root));
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
        assertTrue(t.children(root).isEmpty());
    }

    @Test
    public void testPrepareOverride() throws Exception {
        final Path child = new Path("/t/c", Path.FILE_TYPE);
        final Path root = new Path("/t", Path.DIRECTORY_TYPE) {
        };
        root.setLocal(new NullLocal(null, "l") {
            @Override
            public boolean exists() {
                return true;
            }
        });
        final Transfer t = new DownloadTransfer(new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(child);
                return children;
            }
        }, root) {
            @Override
            protected void transfer(final Path file, final TransferPathFilter filter,
                                    final TransferOptions options) throws BackgroundException {
                if(file.equals(root)) {
                    assertTrue(this.cache().containsKey(root.getReference()));
                }
                super.transfer(file, filter, options);
                if(file.equals(root)) {
                    assertFalse(this.cache().containsKey(root.getReference()));
                }
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
    public void testExclude() throws Exception {
        final Path parent = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), parent);
        t.setSelected(null, false);
    }

    @Test
    public void testCancel() throws Exception {
        final Path parent = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), parent);
        // test cancel while in progress
    }

    @Test
    public void testPrepareDownloadOverrideFilter() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/transfer", Path.DIRECTORY_TYPE);
        test.setLocal(new NullLocal(UUID.randomUUID().toString(), "transfer"));
        final Transfer transfer = new DownloadTransfer(session, test);
        transfer.prepare(test, new TransferStatus().exists(true), new OverwriteFilter(new DownloadSymlinkResolver(Collections.singletonList(test)))
        );
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        assertEquals(status, transfer.getStatus(test));
        final TransferStatus expected = new TransferStatus();
        expected.setResume(false);
        expected.setLength(5L);
        expected.setCurrent(0L);
        assertEquals(expected, transfer.getStatus(new Path("/transfer/test", Path.FILE_TYPE)));
    }

    @Test
    public void testPrepareDownloadResumeFilter() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
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
        transfer.prepare(test, new TransferStatus().exists(true), new ResumeFilter(new DownloadSymlinkResolver(Collections.singletonList(test)))
        );
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        assertEquals(status, transfer.getStatus(test));
        final TransferStatus expected = new TransferStatus();
        expected.setResume(true);
        expected.setCurrent("test".getBytes().length);
        // Remote size
        expected.setLength(5L);
        assertEquals(expected, transfer.getStatus(new Path("/transfer/test", Path.FILE_TYPE)));
        local.delete();
    }

    @Test
    public void testAction() throws Exception {
        final Path parent = new Path("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(new NullSession(new Host("t")), parent);
        assertEquals(TransferAction.ACTION_CALLBACK, t.action(false, false));
        assertEquals(TransferAction.ACTION_CALLBACK, t.action(false, true));
        assertEquals(TransferAction.ACTION_RESUME, t.action(true, false));
    }
}
