package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class SwiftMultipleDeleteFeatureTest extends AbstractSwiftTest {

    @Test(expected = NotfoundException.class)
    @Ignore
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteMultiple() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        for(Location.Name region : new SwiftLocationFeature(session).getLocations()) {
            container.attributes().setRegion(region.getIdentifier());
            new SwiftListService(session, new SwiftRegionService(session)).list(container, new ListProgressListener() {
                @Override
                public void chunk(final Path folder, final AttributedList<Path> list) {
                    try {
                        new SwiftMultipleDeleteFeature(session).delete(list.toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
                    }
                    catch(BackgroundException e) {
                        fail(e.getDetail());
                    }
                }

                @Override
                public ListProgressListener reset() {
                    return this;
                }

                @Override
                public void message(final String message) {
                    //
                }
            });
        }
    }
}
