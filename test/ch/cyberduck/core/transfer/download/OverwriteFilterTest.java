package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        WorkspaceApplicationLauncher.register();
    }

    @Test
    public void testAccept() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.setLocal(new NullLocal(null, "a"));
        p.attributes().setSize(8L);
        assertTrue(f.accept(new NullSession(new Host("h")), p));
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("/", "a") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }));
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("/", "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }));
    }

    @Test
    public void testPrepare() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.setLocal(new NullLocal(null, "a"));
        p.attributes().setSize(8L);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), p);
        assertEquals(8L, status.getLength(), 0L);
    }
}
