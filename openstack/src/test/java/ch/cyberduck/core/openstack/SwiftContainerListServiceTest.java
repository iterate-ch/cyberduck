package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftContainerListServiceTest extends AbstractSwiftTest {

    @Test
    public void testList() throws Exception {
        final AttributedList<Path> list = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion(null)
        ).list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("IAD");
        assertTrue(list.contains(container));
    }

    @Test
    public void testListLimitRegion() throws Exception {
        final AttributedList<Path> list = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion("IAD")
        ).list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("IAD");
        assertTrue(list.contains(container));
    }
}
