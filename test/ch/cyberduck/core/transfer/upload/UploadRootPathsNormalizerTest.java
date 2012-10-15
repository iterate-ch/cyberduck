package ch.cyberduck.core.transfer.upload;

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
public class UploadRootPathsNormalizerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testNormalize() throws Exception {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<Path> list = new ArrayList<Path>();
        list.add(new NullPath("/a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/f/a");
            }
        });
        list.add(new NullPath("/a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/f/a/b");
            }
        });
        final List<Path> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new NullPath("/a", Path.DIRECTORY_TYPE), normalized.get(0));
    }

    @Test
    public void testNameClash() throws Exception {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<Path> list = new ArrayList<Path>();
        list.add(new NullPath("/a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/f/a");
            }
        });
        list.add(new NullPath("/a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/g/a");
            }
        });
        final List<Path> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        assertEquals(new NullPath("/a", Path.FILE_TYPE), normalized.get(0));
        assertEquals(new NullPath("/a-1", Path.FILE_TYPE), normalized.get(1));
    }
}