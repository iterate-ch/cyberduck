package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;
import ch.cyberduck.ui.cocoa.UserDefaultsDateFormatter;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        UserDefaultsDateFormatter.register();
        WorkspaceApplicationLauncher.register();
    }

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
                    assertEquals(String.format("t (%s)", UserDateFormatterFactory.get().getLongFormat(System.currentTimeMillis(), false)), renamed.getName());
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