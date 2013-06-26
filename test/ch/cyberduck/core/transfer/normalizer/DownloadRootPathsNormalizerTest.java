package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DownloadRootPathsNormalizerTest extends AbstractTestCase {

    @Test
    public void testNormalize() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<Path> list = new ArrayList<Path>();
        list.add(new NullPath("/a", Path.DIRECTORY_TYPE));
        list.add(new NullPath("/a/b", Path.FILE_TYPE));
        final List<Path> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new NullPath("/a", Path.DIRECTORY_TYPE), normalized.get(0));
    }

    @Test
    public void testNameClash() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<Path> list = new ArrayList<Path>();
        final NullPath fa = new NullPath("/f/a", Path.FILE_TYPE);
        list.add(fa);
        {
            Local local = new NullLocal(null, fa.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            };
            fa.setLocal(local);
        }
        final NullPath ga = new NullPath("/g/a", Path.FILE_TYPE);
        list.add(ga);
        {
            Local local = new NullLocal(null, ga.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            };
            ga.setLocal(local);
        }
        final List<Path> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        assertEquals(new NullLocal(null, "/a"), normalized.get(0).getLocal());
        assertEquals(new NullLocal(null, "/a-1"), normalized.get(1).getLocal());
    }
}