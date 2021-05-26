package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureAttributesFinderFeatureTest extends AbstractAzureTest {

    @Test
    public void testFindRoot() throws Exception {
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        f.find(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFind() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotNull(attributes.getETag());
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new AzureDirectoryFeature(session, null).mkdir(container, new TransferStatus());
        final AzureAttributesFinderFeature f = new AzureAttributesFinderFeature(session, null);
        final PathAttributes attributes = f.find(container);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertNotNull(attributes.getETag());
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
