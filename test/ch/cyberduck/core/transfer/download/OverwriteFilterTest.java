package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.attributes().setSize(8L);
        assertTrue(f.accept(p));
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.DIRECTORY_TYPE) {
            final NullLocal t = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

            @Override
            public Local getLocal() {
                return t;
            }
        };
        assertTrue(f.accept(p));
        p.getLocal().mkdir();
        assertFalse(f.accept(p));
    }

    @Test
    public void testPrepare() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.attributes().setSize(8L);
        final TransferStatus status = f.prepare(p);
        assertEquals(8L, status.getLength(), 0L);
    }
}
