package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.test.NullLocal;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
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
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.directory)), new NullLocal("/f/a") {
            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }
        }));
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal("/f/a/b")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new Path("/a", EnumSet.of(Path.Type.directory)), normalized.iterator().next().remote);
    }

    @Test
    public void testNameClash() throws Exception {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal("/f/a")));
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal("/g/a")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        final Iterator<TransferItem> iterator = normalized.iterator();
        assertEquals(new Path("/a", EnumSet.of(Path.Type.file)), iterator.next().remote);
        assertEquals(new Path("/a-1", EnumSet.of(Path.Type.file)), iterator.next().remote);
    }
}