package ch.cyberduck.core;

import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class LoginServiceTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new LoginService(new DisabledLoginController(), new DisabledPasswordStore(), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
        l.login(new NullSession(new Host(Protocol.FTP, "h")));
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final FTPSession session = new FTPSession(host);
        session.open();
        LoginService l = new LoginService(new DisabledLoginController() {
            @Override
            public void warn(final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore(), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }
        );
        try {
            l.login(session);
            fail();
        }
        catch(LoginCanceledException e) {
            assertEquals("Unknown", e.getMessage());
            assertTrue(warned.get());
            throw e;
        }
    }
}
