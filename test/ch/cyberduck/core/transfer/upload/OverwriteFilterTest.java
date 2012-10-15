package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        // Local file does not exist
        assertFalse(f.accept(new NullPath("a", Path.FILE_TYPE)));
        assertFalse(f.accept(new NullPath("a", Path.DIRECTORY_TYPE)));
        assertFalse(f.accept(new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public boolean exists() {
                return true;
            }
        }));
    }
}
