package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CredentialsTest extends AbstractTestCase {

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
        final Local t = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), "id_rsa");
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

    @Test
    public void testValidateEmpty() throws Exception {
        Credentials c = new DefaultCredentials("user", "");
        assertTrue(c.validate(Protocol.FTP));
        assertFalse(c.validate(Protocol.WEBDAV));
        assertFalse(c.validate(Protocol.SFTP));
    }

    @Test
    public void testValidateBlank() throws Exception {
        Credentials c = new DefaultCredentials("user", " ");
        assertTrue(c.validate(Protocol.FTP));
        assertTrue(c.validate(Protocol.WEBDAV));
        assertTrue(c.validate(Protocol.SFTP));
    }

    @Test
    public void testNoConfigure() throws Exception {
        Credentials c = new DefaultCredentials("user", " ");
        c.setIdentity(new NullLocal(null, "t"));
        c.configure(Protocol.SFTP, "t");
        assertEquals("t", c.getIdentity().getName());
    }

    @Test
    public void testConfigureKnownHost() throws Exception {
        Credentials c = new DefaultCredentials("user", " ");
        c.configure(Protocol.SFTP, "version.cyberduck.ch");
        assertNotNull(c.getIdentity());
    }

    @Test
    public void testConfigureDefaultKey() throws Exception {
        Credentials c = new DefaultCredentials("user", " ");
        c.configure(Protocol.SFTP, "t");
        // ssh.authentication.publickey.default.enable
        assertNull(c.getIdentity());
    }
}