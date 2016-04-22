package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.preferences.PreferencesFactory;
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
public class DownloadRootPathsNormalizerTest {

    @Test
    public void testNormalize() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        list.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.directory)), new NullLocal("/", "a")));
        list.add(new TransferItem(new Path("/a/b", EnumSet.of(Path.Type.file)), new NullLocal("/a", "b")));
        final List<TransferItem> normalized = n.normalize(list);
        assertEquals(1, normalized.size());
        assertEquals(new Path("/a", EnumSet.of(Path.Type.directory)), normalized.iterator().next().remote);
    }

    @Test
    public void testNameClash() throws Exception {
        DownloadRootPathsNormalizer n = new DownloadRootPathsNormalizer();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        final Path fa = new Path("/f/a", EnumSet.of(Path.Type.file));
        list.add(new TransferItem(fa, new NullLocal(PreferencesFactory.get().getProperty("tmp.dir"), fa.getName()) {
            @Override
            public boolean exists() {
                return false;
            }
        }));
        {
            final Path ga = new Path("/g/a", EnumSet.of(Path.Type.file));
            list.add(new TransferItem(ga, new NullLocal(PreferencesFactory.get().getProperty("tmp.dir"), ga.getName()) {
                @Override
                public boolean exists() {
                    return false;
                }
            }));
            final List<TransferItem> normalized = n.normalize(list);
            assertEquals(2, normalized.size());
            final Iterator<TransferItem> iterator = normalized.iterator();
            assertEquals(new NullLocal(PreferencesFactory.get().getProperty("tmp.dir"), "a"), iterator.next().local);
            assertEquals(new NullLocal(PreferencesFactory.get().getProperty("tmp.dir"), "a-1"), iterator.next().local);
        }
    }
}