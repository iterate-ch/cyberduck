package ch.cyberduck.core.dav;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DAVFindFeatureTest extends AbstractDAVTest {

    @Test
    public void testFind() throws Exception {
        assertTrue(new DAVFindFeature(session).find(new DefaultHomeFinderService(session).find()));
        assertFalse(new DAVFindFeature(session).find(
            new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory))
        ));
        assertFalse(new DAVFindFeature(session).find(
            new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))
        ));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new DAVFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
