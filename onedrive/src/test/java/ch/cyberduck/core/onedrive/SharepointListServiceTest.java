package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SharepointListServiceTest extends AbstractSharepointTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SharepointListService(session, fileid).list(directory, new DisabledListProgressListener());
    }

    @Test
    public void testListDefault() throws Exception {
        final SharepointListService list = new SharepointListService(session, fileid);
        final Path defaultSite = new Path(list.getDefaultSite()).withAttributes(PathAttributes.EMPTY);
        final AttributedList<Path> containers = list.list(defaultSite);
        assertEquals(2, containers.size());
    }

    @Test
    public void testListRoot() throws Exception {
        final Path root = Home.root();
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(root, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
        for(Path d : list) {
            assertSame(root, d.getParent());
        }
    }

    @Test
    public void testListGroups() throws Exception {
        final Path container = new Path(SharepointListService.GROUPS_CONTAINER,
                EnumSet.of(Path.Type.placeholder, Path.Type.directory));
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(container, new DisabledListProgressListener());
        for(Path group : list) {
            assertSame(container, group.getParent());
        }
    }

    @Test
    public void testListSites() throws Exception {
        final Path container = new Path(SharepointListService.SITES_CONTAINER,
                EnumSet.of(Path.Type.placeholder, Path.Type.directory));
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(container, new DisabledListProgressListener());
        for(Path site : list) {
            assertSame(container, site.getParent());
        }
    }

    @Test
    public void testListDrives() throws Exception {
        final SharepointListService list = new SharepointListService(session, fileid);
        final Path siteWithoutId = new Path(list.getDefaultSite()).withAttributes(PathAttributes.EMPTY);
        final Path directory = new Path(siteWithoutId, SharepointListService.DRIVES_CONTAINER, EnumSet.of(Path.Type.directory));
        final AttributedList<Path> drives = list.list(directory);
        assertFalse(drives.isEmpty());
        for(Path d : drives) {
            assertSame(directory, d.getParent());
        }
    }
}
