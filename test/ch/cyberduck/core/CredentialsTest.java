package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

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
        assertFalse(new DefaultCredentials("a", "b").equals(new DefaultCredentials("a", "c")));
    }

    @Test
    public void testSetIdentity() throws Exception {
        Credentials c = new DefaultCredentials();
        c.setIdentity(new Local("~/.ssh/unknown.rsa"));
        assertFalse(c.isPublicKeyAuthentication());
        final Local t = new Local(PreferencesFactory.get().getProperty("tmp.dir"), "id_rsa");
        LocalTouchFactory.get().touch(t);
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
        assertEquals(null, c.getUsername());
        assertEquals(null, c.getPassword());
    }

    @Test
    public void testValidateEmpty() throws Exception {
        Credentials c = new DefaultCredentials("user", "");
        assertTrue(c.validate(new FTPProtocol(), new LoginOptions()));
        assertFalse(c.validate(new DAVProtocol(), new LoginOptions()));
        assertTrue(c.validate(new SFTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testValidateBlank() throws Exception {
        Credentials c = new DefaultCredentials("user", " ");
        assertTrue(c.validate(new FTPProtocol(), new LoginOptions()));
        assertTrue(c.validate(new DAVProtocol(), new LoginOptions()));
        assertTrue(c.validate(new SFTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginReasonable() {
        Credentials credentials = new Credentials("guest", "changeme");
        assertTrue(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutUsername() {
        Credentials credentials = new Credentials(null,
                PreferencesFactory.get().getProperty("connection.login.anon.pass"));
        assertFalse(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutPass() {
        Credentials credentials = new Credentials("guest", null);
        assertFalse(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutEmptyPass() {
        Credentials credentials = new Credentials("guest", "");
        assertTrue(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginAnonymous1() {
        Credentials credentials = new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name"),
                PreferencesFactory.get().getProperty("connection.login.anon.pass"));
        assertTrue(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    @Test
    public void testLoginAnonymous2() {
        Credentials credentials = new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name"),
                null);
        assertTrue(credentials.validate(new FTPProtocol(), new LoginOptions()));
    }

    /**
     * http://trac.cyberduck.ch/ticket/1204
     */
    @Test
    public void testLogin1204() {
        Credentials credentials = new Credentials("cyberduck.login",
                "1seCret");
        assertTrue(credentials.validate(new FTPProtocol(), new LoginOptions()));
        assertEquals("cyberduck.login", credentials.getUsername());
        assertEquals("1seCret", credentials.getPassword());
    }
}