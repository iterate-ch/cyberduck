package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class SwiftContainerListServiceTest extends AbstractSwiftTest {

    @Test
    public void testList() throws Exception {
        final AttributedList<Path> list = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion(null))
                .list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertTrue(list.contains(container));
    }
}
