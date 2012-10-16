package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class DownloadTransferTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new DownloadTransfer(new NullPath("t", Path.FILE_TYPE));
        t.size = 4L;
        t.transferred = 3L;
        final DownloadTransfer serialized = new DownloadTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testChildren() throws Exception {
        final NullPath root = new NullPath("/t", Path.DIRECTORY_TYPE) {
            @Override
            protected AttributedList<Path> list(AttributedList<Path> children) {
                children.add(new NullPath("/t/c", Path.FILE_TYPE));
                return children;
            }
        };
        Transfer t = new DownloadTransfer(root);
        assertEquals(Collections.singletonList(new NullPath("/t/c", Path.FILE_TYPE)), t.children(root));
    }

    @Test
    public void testExclude() throws Exception {
        final NullPath parent = new NullPath("t", Path.FILE_TYPE);
        Transfer t = new DownloadTransfer(parent);
        t.setSelected(null, false);
//        t.transfer();
    }
}
