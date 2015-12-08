package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class WorkspaceBrowserLauncherTest extends AbstractTestCase {

    @Test
    public void testOpen() throws Exception {
        assertFalse(new WorkspaceBrowserLauncher().open(""));
        assertFalse(new WorkspaceBrowserLauncher().open(null));
    }
}
