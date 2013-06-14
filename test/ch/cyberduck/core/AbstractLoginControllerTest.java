package ch.cyberduck.core;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AbstractLoginControllerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NullKeychain.register();
    }

    @Test(expected = LoginCanceledException.class)
    public void testCheck() throws Exception {
        LoginController c = new AbstractLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, boolean enableKeychain, boolean enablePublicKey, boolean enableAnonymous) throws LoginCanceledException {
                assertEquals(Protocol.SFTP, protocol);
                assertEquals("t", title);
                assertEquals("r. No login credentials could be found in the Keychain.", reason);
                assertTrue(enableKeychain);
                assertTrue(enablePublicKey);
                assertFalse(enableAnonymous);
                throw new LoginCanceledException();
            }
        };
        c.check(new Host(Protocol.SFTP, "h"), "t", "r", true, true, false);
    }

    @Test(expected = LoginCanceledException.class)
    public void testFail() throws Exception {
        final Credentials user = new Credentials("t", "p");
        LoginController c = new AbstractLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, boolean enableKeychain, boolean enablePublicKey, boolean enableAnonymous) throws LoginCanceledException {
                assertEquals(Protocol.WEBDAV, protocol);
                assertEquals(user, credentials);
                assertEquals("r", reason);
                assertEquals("Login failed", title);
                assertTrue(enableKeychain);
                assertFalse(enablePublicKey);
                assertTrue(enableAnonymous);
                throw new LoginCanceledException();
            }
        };
        c.fail(Protocol.WEBDAV, user, "r");
    }
}
