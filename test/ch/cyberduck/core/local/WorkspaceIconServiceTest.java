package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSImage;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class WorkspaceIconServiceTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        WorkspaceIconService.register();
    }

    @Test
    public void testSetProgress() throws Exception {
        final WorkspaceIconService s = (WorkspaceIconService) IconServiceFactory.get();
        final Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal file = new NullLocal(Preferences.instance().getProperty("tmp.dir"),
                        UUID.randomUUID().toString());
                assertFalse(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
                file.touch();
                assertTrue(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
                file.delete();
                return file;
            }
        };
        // Test concurrency as set icon is not thread safe
        repeat(c, 50);
    }
}