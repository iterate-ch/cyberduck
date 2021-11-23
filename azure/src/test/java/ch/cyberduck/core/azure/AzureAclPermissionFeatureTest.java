package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;


@Category(IntegrationTest.class)
public class AzureAclPermissionFeatureTest extends AbstractAzureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFoundContainer() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        final AzureAclPermissionFeature f = new AzureAclPermissionFeature(session, null);
        f.getPermission(container);
    }
}
