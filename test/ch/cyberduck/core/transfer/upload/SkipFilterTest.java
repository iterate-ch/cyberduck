package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SkipFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public boolean exists() {
                return false;
            }
        }));
        assertFalse(f.accept(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public boolean exists() {
                return true;
            }
        }));
    }
}
