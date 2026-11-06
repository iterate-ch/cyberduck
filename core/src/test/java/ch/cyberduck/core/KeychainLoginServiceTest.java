package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class KeychainLoginServiceTest {

    @Test
    public void testAuthenticationPromptUsesBookmarkPasswordLabel() throws Exception {
        for(String label : new String[]{"", "API Key (optional)"}) {
            final Host host = new Host(new TestProtocol(), "host", new Credentials("user"));
            host.setProperty("bookmark.password.label", label);
            final LoginOptions defaults = new LoginOptions(host.getProtocol());
            final AtomicBoolean prompted = new AtomicBoolean();
            final Session session = new NullSession(host) {
                @Override
                public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                    throw new LoginFailureException("Sign out in your browser and sign in to the expected account.");
                }
            };
            final KeychainLoginService service = new KeychainLoginService(new DisabledPasswordStore());
            assertFalse(service.authenticate(session, ProgressListener.noop, new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String username, final String title, final String reason,
                                          final LoginOptions options) {
                    prompted.set(true);
                    assertEquals(label.isEmpty() ? defaults.getPasswordPlaceholder() : label, options.getPasswordPlaceholder());
                    assertTrue(reason.startsWith("Sign out in your browser and sign in to the expected account."));
                    return new Credentials(username, "api-key");
                }
            }, CancelCallback.noop));
            assertTrue(prompted.get());
            assertEquals("api-key", host.getCredentials().getPassword());
            assertEquals(defaults.getPasswordPlaceholder(), host.getProtocol().getPasswordPlaceholder());
        }
    }

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
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        LoginService l = new KeychainLoginService(new DisabledPasswordStore());
        l.authenticate(session, new ProgressListener() {
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
        }, LoginCallback.noop, CancelCallback.noop);
    }


    @Test(expected = LoginCanceledException.class)
    public void testCancel() throws Exception {
        LoginService l = new KeychainLoginService(new DisabledPasswordStore());
        l.validate(new Host(new TestProtocol(), "h"),
                new DefaultX509KeyManager(), LoginCallback.noop, new LoginOptions());
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
        l.validate(host, new DefaultX509KeyManager(), LoginCallback.noop, new LoginOptions(host.getProtocol()));
        assertTrue(keychain.get());
        assertFalse(host.getCredentials().isSaved());
        assertEquals("P", host.getCredentials().getPassword());
    }
}
