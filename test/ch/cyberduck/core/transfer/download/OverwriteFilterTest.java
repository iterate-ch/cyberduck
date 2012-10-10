package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class OverwriteFilterTest {

    @Test
    public void testAccept() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.attributes().setSize(8L);
        assertTrue(f.accept(p));
    }

    @Test
    public void testPrepare() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        p.attributes().setSize(8L);
        f.prepare(p);
        assertEquals(8L, p.status().getLength(), 0L);
    }
}
