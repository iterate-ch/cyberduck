package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class UploadTransferTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

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
                        AttributedList<Local> l = new AttributedList<Local>();
                        l.add(new NullLocal(this.getAbsolute(), "c"));
                        return l;
                    }

                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        };
        Transfer t = new UploadTransfer(root);
        assertTrue(t.children(root).isEmpty());
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
        assertEquals(Collections.singletonList(new NullPath("/t/c", Path.FILE_TYPE)), t.children(root));
    }
}