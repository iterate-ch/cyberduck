package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

@Category(IntegrationTest.class)
public class SwiftMultipleDeleteFeatureTest extends AbstractSwiftTest {

    @Test(expected = NotfoundException.class)
    @Ignore
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.volume));
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
