package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class ResumeFilterTest extends AbstractTestCase {

    @Test
    public void testAcceptExistsTrue() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new NullPath("a", Path.DIRECTORY_TYPE);
        p.attributes().setSize(2L);
        assertFalse(f.accept(p));
        assertFalse(p.status().isResume());
    }

    @Test
    public void testAcceptExistsFalse() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("~/Downloads", "a") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        };
        p.attributes().setSize(2L);
        assertTrue(f.accept(p));
        assertFalse(p.status().isResume());
        assertTrue(f.accept(p));
        assertFalse(p.status().isResume());
    }

    @Test
    public void testPrepareFile() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("~/Downloads", "a") {
                    @Override
                    public Attributes attributes() {
                        return new NullAttributes() {
                            @Override
                            public int getType() {
                                return Path.FILE_TYPE;
                            }

                            @Override
                            public long getSize() {
                                return 1L;
                            }
                        };
                    }
                };
            }
        };
        p.attributes().setSize(2L);
        f.prepare(p);
        assertTrue(p.status().isResume());
        assertEquals(1L, p.status().getCurrent(), 0L);
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new NullPath("a", Path.DIRECTORY_TYPE);
        f.prepare(p);
        assertFalse(p.status().isResume());
    }
}
