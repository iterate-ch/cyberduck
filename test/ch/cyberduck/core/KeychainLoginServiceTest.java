package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class KeychainLoginServiceTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testMessages() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginController(), new DisabledPasswordStore());
        l.login(session, new ProgressListener() {
            int i = 0;

            @Override
            public void message(final String message) {
                if(0 == i) {
                    assertEquals("Authenticating as anonymous", message);
                }
                else if(1 == i) {
                    assertEquals("Login failed", message);
                }
                else {
                    fail();
                }
                i++;
            }
        });
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginController(), new DisabledPasswordStore());
        l.login(new FTPSession(new Host(Protocol.FTP, "h")), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginController() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message,
                             final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        try {
            l.login(session, new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            }
            );
            fail();
        }
        catch(LoginCanceledException e) {
            assertTrue(warned.get());
            throw e;
        }
    }
}
