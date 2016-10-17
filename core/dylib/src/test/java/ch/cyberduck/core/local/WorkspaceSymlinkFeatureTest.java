package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceSymlinkFeatureTest {

    @Test
    public void testSymlink() throws Exception {
        final Local target = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(target);
        final Local symlink = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(symlink.exists());
        new WorkspaceSymlinkFeature().symlink(symlink, target.getAbsolute());
        assertTrue(symlink.exists());
        target.delete();
        symlink.delete();
    }
}
