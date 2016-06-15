package ch.cyberduck.core;

import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginCallbackTest {

    @Test(expected = LoginCanceledException.class)
    public void testCheckFTP() throws Exception {
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(new TestProtocol(), bookmark.getProtocol());
                assertEquals("t", title);
                assertEquals("r", reason);
                assertTrue(options.keychain);
                assertTrue(options.publickey);
                assertFalse(options.anonymous);
                throw new LoginCanceledException();
            }
        };
        final LoginOptions options = new LoginOptions();
        options.keychain = true;
        options.publickey = true;
        options.anonymous = false;
        c.prompt(new Host(new TestProtocol()), new Credentials(), "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testCheckSFTP() throws Exception {
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(new TestProtocol(), bookmark.getProtocol());
                assertEquals("t", title);
                assertEquals("r", reason);
                assertTrue(options.keychain);
                assertTrue(options.publickey);
                assertFalse(options.anonymous);
                throw new LoginCanceledException();
            }
        };
        final LoginOptions options = new LoginOptions();
        options.keychain = true;
        options.publickey = true;
        options.anonymous = false;
        c.prompt(new Host(new TestProtocol()), new Credentials(), "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testFail() throws Exception {
        final Credentials user = new Credentials("t", "p");
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(new TestProtocol(), bookmark.getProtocol());
                assertEquals(user, credentials);
                assertEquals("r", reason);
                assertEquals("t", title);
                assertTrue(options.keychain);
                assertFalse(options.publickey);
                assertTrue(options.anonymous);
                throw new LoginCanceledException();
            }
        };
        c.prompt(new Host(new TestProtocol()), user, "t", "r", new LoginOptions(new TestProtocol()));
    }
}
