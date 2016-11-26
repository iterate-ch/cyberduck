package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class KeychainLoginServiceTest {

    @Test(expected = LoginCanceledException.class)
    public void testMessages() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/basic");
        final Session session = new NullSession(host) {
            @Override
            public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
                throw new LoginCanceledException();
            }
        };
        session.open(new DisabledHostKeyCallback());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback(), new DisabledPasswordStore());
        l.authenticate(session, PathCache.empty(), new ProgressListener() {
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
        }, new DisabledCancelCallback());
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new KeychainLoginService(new DisabledLoginCallback(), new DisabledPasswordStore());
        l.validate(new Host(new TestProtocol(), "h"), "", new LoginOptions());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final Session session = new NullSession(host);
        session.open(new DisabledHostKeyCallback());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message,
                             final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        try {
            l.authenticate(session, PathCache.empty(), new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            }, new DisabledCancelCallback());
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
            public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options)
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
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch", credentials);
        l.validate(host, "m", new LoginOptions(host.getProtocol()).publickey(true));
        assertTrue(keychain.get());
        assertTrue(select.get());
    }

    @Test
    public void testFindPasswordSftp() throws Exception {
        final AtomicBoolean keychain = new AtomicBoolean(false);
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options)
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
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch", credentials);
        l.validate(host, "m", new LoginOptions(host.getProtocol()));
        assertTrue(keychain.get());
        assertFalse(host.getCredentials().isSaved());
        assertEquals("P", host.getCredentials().getPassword());
    }
}
