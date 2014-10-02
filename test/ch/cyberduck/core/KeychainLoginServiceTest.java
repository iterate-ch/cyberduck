package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class KeychainLoginServiceTest extends AbstractTestCase {

    @Test(expected = LoginCanceledException.class)
    public void testMessages() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        LoginService l = new KeychainLoginService(new DisabledLoginController(), new DisabledPasswordStore());
        l.login(session, Cache.<Path>empty(), new ProgressListener() {
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
        }, new DisabledTranscriptListener(), null);
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new KeychainLoginService(new DisabledLoginController(), new DisabledPasswordStore());
        l.login(new FTPSession(new Host(new FTPProtocol(), "h")), Cache.<Path>empty(), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new DisabledTranscriptListener(), null);
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(new FTPProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        LoginService l = new KeychainLoginService(new DisabledLoginController() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message,
                             final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        try {
            l.login(session, Cache.<Path>empty(), new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            }, new DisabledTranscriptListener(), null);
            fail();
        }
        catch(LoginCanceledException e) {
            assertTrue(warned.get());
            throw e;
        }
    }

    @Test
    public void testFindPasswordPrivateKey() throws Exception {
        final AtomicBoolean keychain = new AtomicBoolean(false);
        final AtomicBoolean select = new AtomicBoolean(false);
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                fail();
            }

            @Override
            public Local select() throws LoginCanceledException {
                select.set(true);
                return new NullLocal("t");
            }
        }, new DisabledPasswordStore() {
            @Override
            public String getPassword(String hostname, String user) {
                keychain.set(true);
                assertEquals("t", user);
                return null;
            }
        }
        );
        final Credentials credentials = new Credentials();
        credentials.setUsername("u");
        credentials.setIdentity(new NullLocal("t"));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        l.validate(host, "m", new LoginOptions(host.getProtocol()).publickey(true));
        assertTrue(keychain.get());
        assertTrue(select.get());
    }
}
