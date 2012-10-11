package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;
import ch.cyberduck.ui.DateFormatterFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest {

    @Test
    public void testPrepare() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return false;
                    }

                    @Override
                    public void rename(final AbstractPath renamed) {
                        fail();
                    }
                };
            }
        };
        f.prepare(p);
    }

    @Test
    public void testPrepareRename() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("t", Path.FILE_TYPE) {
            final NullLocal local = new NullLocal(null, "t") {
                @Override
                public boolean exists() {
                    return "t".equals(this.getName());
                }

                @Override
                public void rename(final AbstractPath renamed) {
                    assertEquals(String.format("t (%s)", DateFormatterFactory.instance().getLongFormat(System.currentTimeMillis(), false)), renamed.getName());
                }
            };

            @Override
            public Local getLocal() {
                return local;
            }
        };
        f.prepare(p);
        assertEquals("t", p.getLocal().getName());
    }
}