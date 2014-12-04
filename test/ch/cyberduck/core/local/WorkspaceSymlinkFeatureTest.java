package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class WorkspaceSymlinkFeatureTest extends AbstractTestCase {

    @Test
    public void testSymlink() throws Exception {
        final Local target = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(target);
        final FinderLocal symlink = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(symlink.exists());
        new WorkspaceSymlinkFeature().symlink(symlink, target.getAbsolute());
        assertTrue(symlink.exists());
        target.delete();
        symlink.delete();
    }

    @Ignore
    @Test(expected = NotfoundException.class)
    public void testSymlinkNoTarget() throws Exception {
        final Local target = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        final FinderLocal symlink = new FinderLocal(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(symlink.exists());
        new WorkspaceSymlinkFeature().symlink(symlink, target.getAbsolute());
    }
}
