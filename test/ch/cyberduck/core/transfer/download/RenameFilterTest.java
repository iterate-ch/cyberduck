package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

/**
 * @version $Id:$
 */
public class RenameFilterTest {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return this.getName().equals("t");
                    }
                };
            }
        };
        f.prepare(t);
        assertNotSame("t", t.getName());
    }
}