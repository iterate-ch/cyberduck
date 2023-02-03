package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UploadRootPathsNormalizerTest {

    @Test
    public void testNormalize() {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<>();
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.directory)), new NullLocal(System.getProperty("java.io.tmpdir"), "a") {
            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }
        }));
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal(System.getProperty("java.io.tmpdir"), "a" + File.separator + "b")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        final TransferItem i = normalized.iterator().next();
        assertEquals(new Path("/a", EnumSet.of(Path.Type.directory)), i.remote);
    }

    @Test
    public void testNormalizeLargeSet() {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            final String name = String.format("f-%d", i);
            list.add(new TransferItem(new Path(name, EnumSet.of(Path.Type.file)), new NullLocal(name)));
        }
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1000, normalized.size());
    }

    @Test
    public void testNameClash() {
        UploadRootPathsNormalizer n = new UploadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<>();
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal("/f/a")));
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file)), new NullLocal("/g/a")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(2, normalized.size());
        final Iterator<TransferItem> iterator = normalized.iterator();
        assertEquals(new Path("/a", EnumSet.of(Path.Type.file)), iterator.next().remote);
        assertEquals(new Path("/a-1", EnumSet.of(Path.Type.file)), iterator.next().remote);
    }
}
