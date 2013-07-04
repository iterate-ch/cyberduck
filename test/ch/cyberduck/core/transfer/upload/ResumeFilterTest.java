package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ResumeFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullSession(new Host("h")), new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }));
    }

    @Test
    public void testPrepareFalse() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal(null, "t"));
        t.attributes().setSize(7L);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), t);
        assertFalse(status.isResume());
    }

    @Test
    public void testPrepare() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Path getParent() {
                return new NullPath("/", Path.DIRECTORY_TYPE) {
                    @Override
                    public AttributedList<Path> list() {
                        final NullPath f = new NullPath("t", Path.FILE_TYPE);
                        f.attributes().setSize(7L);
                        return new AttributedList<Path>(Collections.<Path>singletonList(f));
                    }
                };
            }
        };
        t.setLocal(new NullLocal(null, "t"));
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), t);
        assertTrue(status.isResume());
        assertEquals(7L, status.getCurrent());
    }

    @Test
    public void testPrepare0() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal(null, "t"));
        t.attributes().setSize(0L);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), t);
        assertFalse(status.isResume());
        assertEquals(0L, status.getCurrent());
    }
}