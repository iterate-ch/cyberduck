package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSImage;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class WorkspaceIconServiceTest extends AbstractTestCase {

    @Before
    @Override
    public void register() {
        super.register();
        WorkspaceIconService.register();
    }

    @Test
    public void testSetProgress() throws Exception {
        final WorkspaceIconService s = (WorkspaceIconService) IconServiceFactory.instance();
        final NullLocal file = new NullLocal(Preferences.instance().getProperty("tmp.dir"), UUID.randomUUID().toString()) {
            @Override
            public void setIcon(final int progress) {
                //
            }
        };
        assertFalse(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
        file.touch();
        assertTrue(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
        file.delete(false);
    }
}