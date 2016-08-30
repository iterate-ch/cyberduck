package ch.cyberduck.core;

import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import static org.junit.Assert.*;

public class CredentialsTest {

    @Test
    public void testEquals() {
        assertEquals(new Credentials("a", "b"), new Credentials("a", "b"));
        assertNotSame(new Credentials("a", "b"), new Credentials("a", "c"));
        assertFalse(new Credentials("a", "b").equals(new Credentials("a", "c")));
    }

    @Test
    public void testSetIdentity() throws Exception {
        Credentials c = new Credentials();
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
        Credentials c = new Credentials("anonymous", "");
        assertEquals("cyberduck@example.net", c.getPassword());
    }

    @Test
    public void testDefault() throws Exception {
        Credentials c = new Credentials();
        assertEquals(null, c.getUsername());
        assertEquals(null, c.getPassword());
    }

    @Test
    public void testValidateEmpty() throws Exception {
        Credentials c = new Credentials("user", "");
        assertTrue(c.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
        assertFalse(c.validate(new TestProtocol(Scheme.http), new LoginOptions()));
        assertTrue(c.validate(new TestProtocol(Scheme.sftp), new LoginOptions()));
    }

    @Test
    public void testValidateBlank() throws Exception {
        Credentials c = new Credentials("user", " ");
        assertTrue(c.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
        assertTrue(c.validate(new TestProtocol(Scheme.http), new LoginOptions()));
        assertTrue(c.validate(new TestProtocol(Scheme.sftp), new LoginOptions()));
    }

    @Test
    public void testLoginReasonable() {
        Credentials credentials = new Credentials("guest", "changeme");
        assertTrue(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutUsername() {
        Credentials credentials = new Credentials(null,
                PreferencesFactory.get().getProperty("connection.login.anon.pass"));
        assertFalse(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutPass() {
        Credentials credentials = new Credentials("guest", null);
        assertFalse(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    @Test
    public void testLoginWithoutEmptyPass() {
        Credentials credentials = new Credentials("guest", "");
        assertTrue(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    @Test
    public void testLoginAnonymous1() {
        Credentials credentials = new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name"),
                PreferencesFactory.get().getProperty("connection.login.anon.pass"));
        assertTrue(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    @Test
    public void testLoginAnonymous2() {
        Credentials credentials = new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name"),
                null);
        assertTrue(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
    }

    /**
     * http://trac.cyberduck.ch/ticket/1204
     */
    @Test
    public void testLogin1204() {
        Credentials credentials = new Credentials("cyberduck.login",
                "1seCret");
        assertTrue(credentials.validate(new TestProtocol(Scheme.ftp), new LoginOptions()));
        assertEquals("cyberduck.login", credentials.getUsername());
        assertEquals("1seCret", credentials.getPassword());
    }
}