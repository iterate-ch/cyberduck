package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class ResumeFilterTest {

    @Test
    public void testAccept() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullPath("t", Path.FILE_TYPE)));
    }

    @Test
    public void testPrepare() throws Exception {
        ResumeFilter f = new ResumeFilter(new NullSymlinkResolver());
        final NullPath t = new NullPath("t", Path.FILE_TYPE);
        t.attributes().setSize(7L);
        f.prepare(t);
        assertTrue(t.status().isResume());
    }
}