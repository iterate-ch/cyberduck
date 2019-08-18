package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphFileIdProviderTest extends AbstractOneDriveTest {

    @Test
    public void testFileIdCollision() throws Exception {
        final Path path2R = new Path("/2R", EnumSet.of(Path.Type.directory));
        final Path path33 = new Path("/33", EnumSet.of(Path.Type.directory));

        final Directory directoryFeature = session._getFeature(Directory.class);
        final Path path2RWithId = directoryFeature.mkdir(path2R, null, new TransferStatus());
        final Path path33WithId = directoryFeature.mkdir(path33, null, new TransferStatus());

        final IdProvider idProvider = session._getFeature(IdProvider.class);
        final String fileId = idProvider.getFileid(path33, new DisabledListProgressListener());

        assertEquals(fileId, path33WithId.attributes().getVersionId());
        assertNotEquals(fileId, path2RWithId.attributes().getVersionId());
    }

}
