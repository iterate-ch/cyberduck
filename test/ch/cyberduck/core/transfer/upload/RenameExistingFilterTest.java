package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullSession(new Host("h")) {
            @Override
            public boolean isRenameSupported(final Path file) {
                return true;
            }

        }, new NullPath("t", Path.FILE_TYPE)
        )
        );
        assertFalse(f.accept(new NullSession(new Host("h")) {
            @Override
            public boolean isRenameSupported(final Path file) {
                return false;
            }
        }, new NullPath("t", Path.FILE_TYPE))
        );
    }

    @Test
    public void testPrepare() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("t", Path.FILE_TYPE) {

            @Override
            public Path getParent() {
                return new NullPath("p", Path.DIRECTORY_TYPE) {
                };
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
                l.add(new NullPath("t", Path.FILE_TYPE));
                return l;
            }
        }, p);
    }
}
