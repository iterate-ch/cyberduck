package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

@Ignore
@Category(IntegrationTest.class)
public class SwiftDistributionPurgeFeatureTest extends AbstractSwiftTest {

    @Test(expected = InteroperabilityException.class)
    public void testInvalidateContainer() throws Exception {
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("IAD");
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(container), new DisabledLoginCallback());
    }

    @Test
    public void testInvalidateFile() throws Exception {
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("IAD");
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))), new DisabledLoginCallback());
    }
}
