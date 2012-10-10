package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SkipFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }));
        assertFalse(f.accept(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }));
    }

    @Test
    public void testPrepare() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver());
        final NullPath a = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        };
        f.prepare(a);
        assertFalse(a.status().isComplete());
        final NullPath b = new NullPath("b", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("a", "b") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        };
        f.prepare(b);
        assertTrue(b.status().isComplete());
    }
}
