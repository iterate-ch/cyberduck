package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.AbstractTestCase;
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
public class DownloadRootPathsNormalizerTest extends AbstractTestCase {

    @Test
    public void testNormalize() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        list.add(new TransferItem(new Path("/a", Path.DIRECTORY_TYPE), new NullLocal("/", "a")));
        list.add(new TransferItem(new Path("/a/b", Path.FILE_TYPE), new NullLocal("/a", "b")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new Path("/a", Path.DIRECTORY_TYPE), normalized.iterator().next().remote);
    }

    @Test
    public void testNameClash() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        final Path fa = new Path("/f/a", Path.FILE_TYPE);
        list.add(new TransferItem(fa, new NullLocal("/", fa.getName()) {
            @Override
            public boolean exists() {
                return false;
            }
        }));
        {
            final Path ga = new Path("/g/a", Path.FILE_TYPE);
            list.add(new TransferItem(ga, new NullLocal("/", ga.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            }));
            final List<TransferItem> normalized = n.normalize(list);
            assertEquals(2, normalized.size());
            final Iterator<TransferItem> iterator = normalized.iterator();
            assertEquals(new NullLocal("/", "a"), iterator.next().local);
            assertEquals(new NullLocal("/", "a-1"), iterator.next().local);
        }
    }
}