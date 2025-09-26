package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPSymlinkFeatureTest extends AbstractSFTPTest {

    @Test
    public void testSymlink() throws Exception {
        final SFTPHomeDirectoryService workdir = new SFTPHomeDirectoryService(session);
        final Path target = new Path(workdir.find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), target, new TransferStatus());
        final Path link = new Path(workdir.find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new SFTPSymlinkFeature(session).symlink(link, target.getName());
        assertTrue(new SFTPFindFeature(session).find(link));
        assertEquals(EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink),
            new SFTPListService(session).list(workdir.find(), new DisabledListProgressListener()).get(link).getType());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(link), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SFTPFindFeature(session).find(link));
        assertTrue(new SFTPFindFeature(session).find(target));
        new SFTPDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
