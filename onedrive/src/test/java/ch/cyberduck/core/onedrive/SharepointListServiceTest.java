package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.PathAttributesHomeFeature;
import ch.cyberduck.core.shared.RootPathContainerService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.onedrive.SharepointListService.DRIVES_CONTAINER;
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
    public void testListRoot() throws Exception {
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(Home.root(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
    }

    @Test
    public void testListDefault() throws Exception {
        new SharepointListService(session, fileid).list(SharepointListService.DEFAULT_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListDefaultDriveOverwrite() throws Exception {
        final ListService list = new SharepointListService(session, fileid);
        final AttributedList<Path> drives = list.list(new Path(SharepointListService.DEFAULT_NAME, DRIVES_CONTAINER, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        final Path drive = drives.get(0);
        new PathAttributesHomeFeature(session, () -> drive, new GraphAttributesFinderFeature(session, fileid), new RootPathContainerService()).find();
        list.list(drive, new DisabledListProgressListener());
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
        final ListService list = new SharepointListService(session, fileid);
        final Path defaultSite = list.list(Home.root(), new DisabledListProgressListener()).find(new SimplePathPredicate(SharepointListService.DEFAULT_NAME));
        final Path drives = list.list(defaultSite).find(new SimplePathPredicate(new Path(defaultSite, SharepointListService.DRIVES_NAME.getName(), EnumSet.of(Path.Type.directory))));
        list.list(drives);
    }
}
