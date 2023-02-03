package ch.cyberduck.core.dav;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DAVFindFeatureTest extends AbstractDAVTest {

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new DAVFindFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory))));
        assertFalse(new DAVFindFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFind() throws Exception {
        assertTrue(new DAVFindFeature(session).find(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new DAVFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
