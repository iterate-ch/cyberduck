package ch.cyberduck.core.azure;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;


@Category(IntegrationTest.class)
public class AzureAclPermissionFeatureTest extends AbstractAzureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFoundContainer() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        final AzureAclPermissionFeature f = new AzureAclPermissionFeature(session);
        f.getPermission(container);
    }

    @Test
    public void testWriteContainer() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final AzureAclPermissionFeature f = new AzureAclPermissionFeature(session);
        {
            final Acl acl = new Acl();
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
            f.setPermission(container, acl);
            assertEquals(acl, f.getPermission(container));
        }
        {
            final Acl acl = new Acl();
            f.setPermission(container, acl);
            assertEquals(acl, f.getPermission(container));
        }
    }
}
