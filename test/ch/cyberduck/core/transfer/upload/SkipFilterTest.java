package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SkipFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertTrue(f.accept(new Path("a", Path.FILE_TYPE), new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus()));
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertFalse(f.accept(new Path("a", Path.FILE_TYPE), new NullLocal("a") {
            @Override
            public boolean exists() {
                return false;
            }
        }, new TransferStatus()
        ));
    }
}
