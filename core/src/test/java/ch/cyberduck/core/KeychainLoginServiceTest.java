package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
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
            public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                throw new LoginCanceledException();
            }
        };
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        LoginService l = new KeychainLoginService(new DisabledPasswordStore());
        l.authenticate(new DisabledProxyFinder(), session, new ProgressListener() {
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
        }, new DisabledLoginCallback(), new DisabledCancelCallback());
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new KeychainLoginService(new DisabledPasswordStore());
        l.validate(new Host(new TestProtocol(), "h"), new DisabledLoginCallback(), new LoginOptions());
    }

    @Test
    public void testFindPasswordSftp() throws Exception {
        final AtomicBoolean keychain = new AtomicBoolean(false);
        KeychainLoginService l = new KeychainLoginService(new DisabledPasswordStore() {
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
        l.validate(host, new DisabledLoginCallback(), new LoginOptions(host.getProtocol()));
        assertTrue(keychain.get());
        assertFalse(host.getCredentials().isSaved());
        assertEquals("P", host.getCredentials().getPassword());
    }
}
