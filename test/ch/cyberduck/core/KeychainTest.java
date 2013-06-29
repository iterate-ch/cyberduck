package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class KeychainTest extends AbstractTestCase {

    @Test
    public void testFindPassword() throws Exception {
        final PasswordStore k = new Keychain();
        assertNull(k.getPassword("cyberduck.ch", "u"));
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }
}
