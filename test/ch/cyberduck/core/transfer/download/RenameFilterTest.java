package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class RenameFilterTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        WorkspaceApplicationLauncher.register();
    }

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return this.getName().equals("/t");
                    }
                };
            }
        };
        f.prepare(t);
        assertNotSame("/t", t.getName());
    }
}