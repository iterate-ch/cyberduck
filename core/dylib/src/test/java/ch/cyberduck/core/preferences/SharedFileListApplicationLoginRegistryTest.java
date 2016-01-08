package ch.cyberduck.core.preferences;

import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.DisabledApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SharedFileListApplicationLoginRegistryTest {

    @Test
    public void testRegister() throws Exception {
        assertTrue(new SharedFileListApplicationLoginRegistry(new LaunchServicesApplicationFinder()).register(new Application("ch.sudo.cyberduck")));
    }

    @Test
    public void testRegisterInvalidBundle() throws Exception {
        assertFalse(new SharedFileListApplicationLoginRegistry(new DisabledApplicationFinder()).register(new Application("ch.sudo.cyberduck")));
    }
}