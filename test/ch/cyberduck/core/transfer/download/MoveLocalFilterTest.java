package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.NullSymlinkResolver;

import org.junit.Test;

/**
 * @version $Id:$
 */
public class MoveLocalFilterTest {

    @Test
    public void testPrepare() throws Exception {
        MoveLocalFilter f = new MoveLocalFilter(new NullSymlinkResolver());
        f.prepare(new NullPath("t", Path.FILE_TYPE));
    }
}
