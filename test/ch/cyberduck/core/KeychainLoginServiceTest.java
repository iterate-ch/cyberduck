package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.preferences.PreferencesFactory;
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
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback(), new DisabledPasswordStore());
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
        }, null);
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new KeychainLoginService(new DisabledLoginCallback(), new DisabledPasswordStore());
        l.login(new FTPSession(new Host(new FTPProtocol(), "h")), Cache.<Path>empty(), new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, null);
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(new FTPProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback() {
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
            }, null);
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
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                fail();
            }

            @Override
            public Local select(final Local identity) throws LoginCanceledException {
                select.set(true);
                return identity;
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
        credentials.setIdentity(new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        });
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        l.validate(host, "m", new LoginOptions(host.getProtocol()).publickey(true));
        assertTrue(keychain.get());
        assertTrue(select.get());
    }

    @Test
    public void testFindPasswordSftp() throws Exception {
        final AtomicBoolean keychain = new AtomicBoolean(false);
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                fail();
            }
        }, new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                keychain.set(true);
                return "P";
            }
        }
        );
        final Credentials credentials = new Credentials();
        credentials.setUsername("u");
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        l.validate(host, "m", new LoginOptions(host.getProtocol()));
        assertTrue(keychain.get());
        assertFalse(host.getCredentials().isSaved());
        assertEquals("P", host.getCredentials().getPassword());
    }
}
