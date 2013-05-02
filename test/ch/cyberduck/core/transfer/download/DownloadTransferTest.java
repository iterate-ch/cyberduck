package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DownloadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() {
        Transfer t = new DownloadTransfer(new NullPath("t", Path.FILE_TYPE));
        t.addSize(4L);
        t.addTransferred(3L);
        final DownloadTransfer serialized = new DownloadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
        assertFalse(serialized.isComplete());
    }

    @Test
    public void testSerializeComplete() {
        Transfer t = new DownloadTransfer(new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }) {
            @Override
            protected void fireWillTransferPath(Path path) {
                assertEquals(new NullPath("/t", Path.DIRECTORY_TYPE), path);
            }

            @Override
            protected void fireDidTransferPath(Path path) {
                assertEquals(new NullPath("/t", Path.DIRECTORY_TYPE), path);
            }
        };
        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() {
                return TransferAction.ACTION_OVERWRITE;
            }
        }, new TransferOptions());
        assertTrue(t.isComplete());
        final DownloadTransfer serialized = new DownloadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertTrue(serialized.isComplete());
    }

    @Test
    public void testChildren() {
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            protected AttributedList<Path> list(AttributedList<Path> children) {
                children.add(new NullPath("/t/c", Path.FILE_TYPE));
                return children;
            }
        };
        root.setLocal(new NullLocal(null, "l"));
        Transfer t = new DownloadTransfer(root);
        assertEquals(Collections.singletonList(new NullPath("/t/c", Path.FILE_TYPE)), t.children(root));
    }

    @Test(expected = NullPointerException.class)
    public void testDownloadPath() {
        final NullPath root = new NullPath("/t", Path.FILE_TYPE);
        assertNull(root.getLocal());
        List<Path> roots = new ArrayList<Path>();
        roots.add(root);
        roots.add(root);
        Transfer t = new DownloadTransfer(roots);
    }

    @Test
    public void testCustomDownloadPath() {
        final NullPath root = new NullPath("/t", Path.FILE_TYPE);
        final NullLocal l = new NullLocal(null, "n");
        root.setLocal(l);
        assertNotNull(root.getLocal());
        Transfer t = new DownloadTransfer(root);
        assertEquals(l, root.getLocal());
    }

    @Test
    public void testExclude() throws Exception {
        final NullPath parent = new NullPath("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(parent);
        t.setSelected(null, false);
    }
}
