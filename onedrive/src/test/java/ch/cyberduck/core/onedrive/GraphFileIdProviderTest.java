package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphFileIdProviderTest extends AbstractOneDriveTest {

    @Test
    public void testFileIdCollision() throws Exception {
        final Path home = new OneDriveHomeFinderService().find();
        final Path path2R = new Path(home, "/2R", EnumSet.of(Path.Type.directory));
        final Path path33 = new Path(home, "/33", EnumSet.of(Path.Type.directory));
        try {
            new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(path2R), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
        catch(NotfoundException e) {
            //
        }
        try {
            new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(path33), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
        catch(NotfoundException e) {
            //
        }
        final Directory directoryFeature = new GraphDirectoryFeature(session, fileid);
        final Path path2RWithId = directoryFeature.mkdir(path2R, new TransferStatus());
        assertNotNull(path2RWithId.attributes().getFileId());
        final Path path33WithId = directoryFeature.mkdir(path33, new TransferStatus());
        assertNotNull(path33WithId.attributes().getFileId());
        assertNotEquals(path2RWithId.attributes().getFileId(), path33WithId.attributes().getFileId());

        final GraphFileIdProvider idProvider = fileid;
        final String fileId = idProvider.getFileId(path33);

        assertEquals(fileId, path33WithId.attributes().getFileId());
        assertNotEquals(fileId, path2RWithId.attributes().getFileId());

        new GraphDeleteFeature(session, fileid).delete(Arrays.asList(path2RWithId, path33WithId), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
