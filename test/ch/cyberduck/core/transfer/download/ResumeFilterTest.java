package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ResumeFilterTest extends AbstractTestCase {

    @Test
    public void testAcceptDirectory() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new Path("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("d", "a");
            }
        };
        assertTrue(f.accept(new NullSession(new Host("h")), p));
    }

    @Test
    public void testAcceptExistsFalse() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new Path("a", Path.FILE_TYPE) {
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
        assertTrue(f.accept(new NullSession(new Host("h")), p));
    }

    @Test
    public void testPrepareFile() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new Path("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal("~/Downloads", "a") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
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
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), p, new TransferStatus());
        assertTrue(status.isResume());
        assertEquals(1L, status.getCurrent(), 0L);
    }

    @Test
    public void testPrepareDirectoryExists() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new Path("a", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal(null, "a"));
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), p, new TransferStatus());
        assertTrue(status.isResume());
    }

    @Test
    public void testPrepareDirectoryExistsFalse() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        Path p = new Path("a", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal(null, "a") {
            @Override
            public boolean exists() {
                return false;
            }
        });
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), p, new TransferStatus());
        assertFalse(status.isResume());
    }
}
