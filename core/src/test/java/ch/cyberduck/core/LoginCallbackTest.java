package ch.cyberduck.core;

import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginCallbackTest {

    @Test(expected = LoginCanceledException.class)
    public void testCheckFTP() throws Exception {
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("username", username);
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
        c.prompt(new Host(new TestProtocol()), "username", "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testCheckSFTP() throws Exception {
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("username", username);
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
        c.prompt(new Host(new TestProtocol()), "username", "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testFail() throws Exception {
        LoginCallback c = new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("username", username);
                assertEquals("r", reason);
                assertEquals("t", title);
                assertTrue(options.keychain);
                assertFalse(options.publickey);
                assertTrue(options.anonymous);
                throw new LoginCanceledException();
            }
        };
        c.prompt(new Host(new TestProtocol()), "username", "t", "r", new LoginOptions(new TestProtocol()));
    }
}
