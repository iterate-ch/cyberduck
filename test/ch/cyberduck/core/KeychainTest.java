package ch.cyberduck.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @version $Id:$
 */
public class KeychainTest extends AbstractTestCase {

    @Before
    @Override
    public void register() {
        super.register();
        Keychain.register();
    }

    @Test
    public void testFind() throws Exception {
        PasswordStore k = KeychainFactory.instance();
        assertNull(k.getPassword("cyberduck.ch", "u"));
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }
}
