package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class UploadRootPathsNormalizerTest extends AbstractTestCase {

    @Test
    public void testNormalize() throws Exception {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        list.add(new TransferItem(new Path("/a", Path.DIRECTORY_TYPE), new NullLocal("/f/a") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("/f/a") {
                    @Override
                    public boolean isDirectory() {
                        return true;
                    }
                };
            }
        }));
        list.add(new TransferItem(new Path("/a", Path.FILE_TYPE), new NullLocal("/f/a/b")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new Path("/a", Path.DIRECTORY_TYPE), normalized.iterator().next().remote);
    }

    @Test
    public void testNameClash() throws Exception {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        list.add(new TransferItem(new Path("/a", Path.FILE_TYPE), new NullLocal("/f/a")));
        list.add(new TransferItem(new Path("/a", Path.FILE_TYPE), new NullLocal("/g/a")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        final Iterator<TransferItem> iterator = normalized.iterator();
        assertEquals(new Path("/a", Path.FILE_TYPE), iterator.next().remote);
        assertEquals(new Path("/a-1", Path.FILE_TYPE), iterator.next().remote);
    }
}