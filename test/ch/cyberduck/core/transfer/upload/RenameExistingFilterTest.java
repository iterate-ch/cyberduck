package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal("/Downloads", "n"));
        assertTrue(f.accept(new NullSession(new Host("h")), t));
    }

    @Test
    public void testPrepare() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final Path p = new Path("t", Path.FILE_TYPE) {

            @Override
            public Path getParent() {
                return new Path("p", Path.DIRECTORY_TYPE);
            }
        };
        p.setLocal(new NullLocal("/Downloads", "n"));
        f.prepare(new NullSession(new Host("h")) {
            @Override
            public void rename(final Path file, final Path renamed) {
                assertNotSame(file.getName(), renamed.getName());
            }

            @Override
            public AttributedList<Path> list(final Path file) {
                final AttributedList<Path> l = new AttributedList<Path>();
                l.add(new Path("t", Path.FILE_TYPE));
                return l;
            }
        }, p, new ch.cyberduck.core.transfer.TransferStatus());
    }
}
