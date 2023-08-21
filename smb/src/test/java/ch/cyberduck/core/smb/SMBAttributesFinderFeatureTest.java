package ch.cyberduck.core.smb;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBAttributesFinderFeatureTest extends AbstractSMBTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SMBAttributesFinderFeature f = new SMBAttributesFinderFeature(session);
        f.find(test);
    }

    @Test
    public void testFindFile() throws Exception {
        final Path test = new SMBTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SMBAttributesFinderFeature f = new SMBAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path test = new SMBDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final SMBAttributesFinderFeature f = new SMBAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertNotEquals(-1L, attributes.getModificationDate());
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
    public void testFindNoPropfind() throws Exception {
        final SMBAttributesFinderFeature f = new SMBAttributesFinderFeature(session);
        final Path file = new Path("/userTest.txt", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertNotNull(attributes);
    }
}
