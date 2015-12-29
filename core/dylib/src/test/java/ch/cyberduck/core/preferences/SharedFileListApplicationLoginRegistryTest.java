package ch.cyberduck.core.preferences;

import ch.cyberduck.core.local.Application;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SharedFileListApplicationLoginRegistryTest {

    @Test
    public void testRegister() throws Exception {
        assertTrue(new SharedFileListApplicationLoginRegistry().register(new Application("ch.sudo.cyberduck")));
    }
}