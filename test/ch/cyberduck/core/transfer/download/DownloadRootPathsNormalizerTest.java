package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DownloadRootPathsNormalizerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

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
        list.add(new NullPath("/f/a", Path.FILE_TYPE) {
            private Local local = new NullLocal(null, this.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            };

            @Override
            public Local getLocal() {
                return local;
            }
        });
        list.add(new NullPath("/g/a", Path.FILE_TYPE) {
            private Local local = new NullLocal(null, this.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            };

            @Override
            public Local getLocal() {
                return local;
            }
        });
        final List<Path> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        assertEquals(new NullLocal(null, "/a"), normalized.get(0).getLocal());
        assertEquals(new NullLocal(null, "/a-1"), normalized.get(1).getLocal());
    }
}