package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SFTPFindFeatureTest extends AbstractSFTPTest {

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new SFTPFindFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDirectory() throws Exception {
        assertTrue(new SFTPFindFeature(session).find(new SFTPHomeDirectoryService(session).find()));
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new SFTPFindFeature(session).find(file));
        assertFalse(new SFTPFindFeature(session).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new SFTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SFTPFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
