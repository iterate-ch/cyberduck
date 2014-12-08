package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class WorkspaceBrowserLauncherTest extends AbstractTestCase {

    @Test
    public void testOpen() throws Exception {
        assertTrue(new WorkspaceBrowserLauncher().open("http://cyberduck.ch"));
        assertFalse(new WorkspaceBrowserLauncher().open(""));
        assertFalse(new WorkspaceBrowserLauncher().open(null));
    }
}
