package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AbstractLoginControllerTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testCheckFTP() throws Exception {
        LoginController c = new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(Protocol.FTP, protocol);
                assertEquals("t", title);
                assertEquals("r.", reason);
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
        c.prompt(Protocol.FTP, new Credentials(), "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testCheckSFTP() throws Exception {
        LoginController c = new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(Protocol.SFTP, protocol);
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
        c.prompt(Protocol.SFTP, new Credentials(), "t", "r", options);
    }

    @Test(expected = LoginCanceledException.class)
    public void testFail() throws Exception {
        final Credentials user = new Credentials("t", "p");
        LoginController c = new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals(Protocol.WEBDAV, protocol);
                assertEquals(user, credentials);
                assertEquals("r", reason);
                assertEquals("t", title);
                assertTrue(options.keychain);
                assertFalse(options.publickey);
                assertTrue(options.anonymous);
                throw new LoginCanceledException();
            }
        };
        c.prompt(Protocol.WEBDAV, user, "t", "r");
    }
}
