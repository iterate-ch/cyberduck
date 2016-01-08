package ch.cyberduck.core.preferences;

import ch.cyberduck.core.local.Application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SharedFileListApplicationLoginRegistryTest {

    @Test
    public void testRegister() throws Exception {
        assertTrue(new SharedFileListApplicationLoginRegistry().register(new Application("ch.sudo.cyberduck")));
    }

    @Test
    public void testRegisterInvalidBundle() throws Exception {
        assertFalse(new SharedFileListApplicationLoginRegistry().register(new Application("_ch.sudo.cyberduck")));
    }
}