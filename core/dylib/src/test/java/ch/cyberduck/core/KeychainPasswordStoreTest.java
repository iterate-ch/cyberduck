package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class KeychainPasswordStoreTest {

    @Test
    public void testFindPassword() {
        final KeychainPasswordStore k = new KeychainPasswordStore();
        assertNull(k.getPassword("cyberduck.ch", "u"));
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }
}
