package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import java.util.EnumSet;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(Home.root(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
    }

    @Test
    public void testListGroups() throws Exception {
        new SharepointListService(session, fileid).list(SharepointListService.GROUPS_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListSites() throws Exception {
        new SharepointListService(session, fileid).list(SharepointListService.SITES_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListDrives() throws Exception {
        final SharepointListService list = new SharepointListService(session, fileid);
        final Path siteWithoutId = new Path(list.getDefaultSite()).withAttributes(PathAttributes.EMPTY);
        final AttributedList<Path> drives = list.list(new Path(siteWithoutId, SharepointListService.DRIVES_CONTAINER, EnumSet.of(AbstractPath.Type.directory)));
        assertFalse(drives.isEmpty());
    }
}
