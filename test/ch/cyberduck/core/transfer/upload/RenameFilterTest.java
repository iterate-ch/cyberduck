package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class RenameFilterTest extends AbstractTestCase {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new NullSymlinkResolver());
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal(null, "t"));
        f.prepare(new NullSession(new Host("h")) {
            @Override
            public boolean exists(final Path path) throws BackgroundException {
                return path.getName().equals("t");
            }
        }, t);
        assertNotSame("t", t.getName());
    }
}