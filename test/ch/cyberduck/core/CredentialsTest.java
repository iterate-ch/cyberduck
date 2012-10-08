package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CredentialsTest {

    @Test
    public void testEquals() {
        assertEquals(new DefaultCredentials("a", "b"), new DefaultCredentials("a", "b"));
        assertNotSame(new DefaultCredentials("a", "b"), new DefaultCredentials("a", "c"));
    }

    @Test
    public void testSetIdentity() throws Exception {
        Credentials c = new DefaultCredentials();
        c.setIdentity(LocalFactory.createLocal("~/.ssh/unknown.rsa"));
        assertFalse(c.isPublicKeyAuthentication());
        final Local t = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), "~/.ssh/id_rsa");
        t.touch();
        c.setIdentity(t);
        assertTrue(c.isPublicKeyAuthentication());
        t.delete();
    }

    @Test
    public void testAnonymous() throws Exception {
        Credentials c = new DefaultCredentials("anonymous", "");
        assertEquals("cyberduck@example.net", c.getPassword());
    }

    @Test
    public void testDefault() throws Exception {
        Credentials c = new DefaultCredentials();
        assertEquals(System.getProperty("user.name"), c.getUsername());
        assertEquals(null, c.getPassword());
    }
}
