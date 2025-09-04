package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class SFTPAttributesFinderFeatureTest extends AbstractSFTPTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        new SFTPAttributesFinderFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new SFTPDirectoryFeature(session).mkdir(new SFTPWriteFeature(session), test, new TransferStatus());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertNotNull(attributes);
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }

    @Test
    public void testFindSymbolicLink() throws Exception {
        final Path file = new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path symlink = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPSymlinkFeature(session).symlink(symlink, file.getAbsolute());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(symlink);
        assertNotNull(attributes);
    }
}
