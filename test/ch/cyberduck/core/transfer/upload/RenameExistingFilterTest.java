package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.*;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host("t")) {
                    @Override
                    public boolean isRenameSupported(final Path file) {
                        return true;
                    }
                };
            }

            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        })
        );
        assertFalse(f.accept(new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host("t")) {
                    @Override
                    public boolean isRenameSupported(final Path file) {
                        return false;
                    }
                };
            }

            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        })
        );
    }

    @Test
    public void testPrepare() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host(new NullProtocol(), "t"));
            }

            @Override
            public Path getParent() {
                return new NullPath("p", Path.DIRECTORY_TYPE) {
                    @Override
                    protected AttributedList<Path> list(final AttributedList<Path> children) {
                        final AttributedList<Path> l = new AttributedList<Path>();
                        l.add(new NullPath("t", Path.FILE_TYPE));
                        return l;
                    }

                    @Override
                    public void rename(final AbstractPath renamed) {
                        assertNotSame(this.getName(), renamed.getName());
                    }
                };
            }
        };
        p.setLocal(new NullLocal("/Downloads", "n"));
        f.prepare(p);
    }
}
