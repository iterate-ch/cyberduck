package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.ui.cocoa.application.NSImage;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class WorkspaceIconServiceTest extends AbstractTestCase {

    @Test
    public void testSetProgressNoFile() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(s.update(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
    }

    @Test
    public void testSetProgress() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(file);
        assertTrue(s.update(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
        file.delete();
    }

    @Test
    public void testRemove() throws Exception {
        final WorkspaceIconService s = new WorkspaceIconService();
        final Local file = new Local(PreferencesFactory.get().getProperty("tmp.dir"),
                UUID.randomUUID().toString());
        assertFalse(s.remove(file));
        LocalTouchFactory.get().touch(file);
        assertFalse(s.remove(file));
        assertTrue(s.update(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
        assertTrue(s.remove(file));
        file.delete();
    }
}