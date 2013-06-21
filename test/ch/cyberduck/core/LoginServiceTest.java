package ch.cyberduck.core;

import org.junit.Test;

/**
 * @version $Id:$
 */
public class LoginServiceTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new LoginService(new DisabledLoginController());
        l.login(new NullSession(new Host(Protocol.FTP, "h")));
    }
}
