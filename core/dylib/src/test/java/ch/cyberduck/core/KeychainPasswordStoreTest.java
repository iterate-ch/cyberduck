package ch.cyberduck.core;

import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Ignore
public class KeychainPasswordStoreTest {

    @Test
    public void testFindGenericPassword() throws LocalAccessDeniedException {
        final KeychainPasswordStore k = new KeychainPasswordStore();
        k.deletePassword("cyberduck.ch", "u");
        assertNull(k.getPassword("cyberduck.ch", "u"));
        k.addPassword("cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword("cyberduck.ch", "u"));
        // Duplicate
        k.addPassword("cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword("cyberduck.ch", "u"));
        k.deletePassword("cyberduck.ch", "u");
        assertNull(k.getPassword("cyberduck.ch", "u"));
    }

    @Test
    public void testFindInternetPassword() throws LocalAccessDeniedException {
        final KeychainPasswordStore k = new KeychainPasswordStore();
        k.deletePassword(Scheme.http, 80, "cyberduck.ch", "u");
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        k.addPassword(Scheme.http, 80, "cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        // Duplicate
        k.addPassword(Scheme.http, 80, "cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        k.deletePassword(Scheme.http, 80, "cyberduck.ch", "u");
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }
}
