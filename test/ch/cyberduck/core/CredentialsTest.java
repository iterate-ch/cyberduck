package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class CredentialsTest {

    @Test
    public void testSetIdentity() throws Exception {
        Credentials c = new Credentials() {
            @Override
            public String getUsernamePlaceholder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPasswordPlaceholder() {
                throw new UnsupportedOperationException();
            }
        };
        c.setIdentity(LocalFactory.createLocal("~/.ssh/unknown.rsa"));
        assertFalse(c.isPublicKeyAuthentication());
        c.setIdentity(LocalFactory.createLocal("~/.ssh/id_rsa"));
        assertTrue(c.isPublicKeyAuthentication());
    }

    @Test
    public void testAnonymous() throws Exception {
        Credentials c = new Credentials("anonymous", "") {
            @Override
            public String getUsernamePlaceholder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPasswordPlaceholder() {
                throw new UnsupportedOperationException();
            }
        };
        assertEquals("cyberduck@example.net", c.getPassword());
    }

    @Test
    public void testDefault() throws Exception {
        Credentials c = new Credentials() {
            @Override
            public String getUsernamePlaceholder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPasswordPlaceholder() {
                throw new UnsupportedOperationException();
            }
        };
        assertEquals(System.getProperty("user.name"), c.getUsername());
        assertEquals(null, c.getPassword());
    }
}
