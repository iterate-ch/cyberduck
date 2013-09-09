package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SkipFilterTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        WorkspaceApplicationLauncher.register();
    }

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertTrue(f.accept(new Path("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }, new TransferStatus()));
        assertFalse(f.accept(new Path("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }, new TransferStatus()));
    }
}
