package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
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
            public void login(final Proxy proxy, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                throw new LoginCanceledException();
            }
        };
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback(), new DisabledPasswordStore());
        l.authenticate(Proxy.DIRECT, session, PathCache.empty(), new ProgressListener() {
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
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        LoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message,
                             final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        try {
            l.authenticate(Proxy.DIRECT, session, PathCache.empty(), new ProgressListener() {
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
    public void testFindPasswordSftp() throws Exception {
        final AtomicBoolean keychain = new AtomicBoolean(false);
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                fail();
                return null;
            }
        }, new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
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
