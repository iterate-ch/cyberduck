package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
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

    @Test
    public void getFileIdFile() throws Exception {
        final GraphFileIdProvider nodeid = new GraphFileIdProvider(session);
        final Path home = new OneDriveHomeFinderService().find();
        final String name = String.format("%s", new AlphanumericRandomStringService().random());
        final Path file = new GraphTouchFeature(session, nodeid).touch(new Path(home, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        nodeid.clear();
        final String nodeId = nodeid.getFileId(new Path(home, name, EnumSet.of(Path.Type.file)));
        assertNotNull(nodeId);
        assertEquals(nodeId, nodeid.getFileId(new Path(home.withAttributes(PathAttributes.EMPTY), name, EnumSet.of(Path.Type.file))));
        nodeid.clear();
        assertEquals(nodeId, nodeid.getFileId(new Path(home, StringUtils.upperCase(name), EnumSet.of(Path.Type.file))));
        nodeid.clear();
        assertEquals(nodeId, nodeid.getFileId(new Path(home, StringUtils.lowerCase(name), EnumSet.of(Path.Type.file))));
        try {
            assertNull(nodeid.getFileId(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new GraphDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
