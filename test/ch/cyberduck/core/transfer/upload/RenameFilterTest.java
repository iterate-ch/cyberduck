package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

/**
 * @version $Id:$
 */
public class RenameFilterTest extends AbstractTestCase {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public boolean exists() {
                return this.getName().equals("t");
            }
        };
        f.prepare(t);
        assertNotSame("t", t.getName());
    }
}