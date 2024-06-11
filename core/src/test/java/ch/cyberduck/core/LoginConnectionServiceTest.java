package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class LoginConnectionServiceTest {

    @Test(expected = LoginCanceledException.class)
    public void testNoResolveForHTTPProxy() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol(), "unknownhost.local", new Credentials("user", ""))) {
            @Override
            public boolean isConnected() {
                return false;
            }
        };
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(),
            new DisabledProxyFinder() {
                @Override
                public Proxy find(final String target) {
                    return new Proxy(Proxy.Type.HTTP, "proxy.local", 6666);
                }
            });
        s.check(session, new DisabledCancelCallback());
    }

    @Test
    public void testConnectDnsFailure() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol(), "unknownhost.local", new Credentials("user", "p"))) {
            @Override
            public boolean isConnected() {
                return false;
            }
        };
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new HostKeyCallback() {
            @Override
            public boolean verify(final Host hostname, final PublicKey key) {
                assertEquals(Session.State.opening, session.getState());
                return true;
            }
        }, new DisabledPasswordStore(),
            new DisabledProgressListener()
        );
        try {
            s.check(session, new DisabledCancelCallback());
            fail();
        }
        catch(ResolveFailedException e) {
            assertEquals("Connection failed", e.getMessage());
            assertEquals("DNS lookup for unknownhost.local failed. DNS is the network service that translates a server name to its Internet address. This error is most often caused by having no connection to the Internet or a misconfigured network. It can also be caused by an unresponsive DNS server or a firewall preventing access to the network.", e.getDetail());
            assertEquals(UnknownHostException.class, e.getCause().getClass());
            assertEquals(Session.State.closed, session.getState());
        }
        try {
            s.check(new NullSession(new Host(new TestProtocol(), "localhost", new Credentials("user", ""))) {
                @Override
                public boolean isConnected() {
                    return false;
                }
            }, new DisabledCancelCallback());
            fail();
        }
        catch(LoginCanceledException e) {

        }
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
            new DisabledProgressListener());
        s.check(new NullSession(new Host(new TestProtocol(), "")), new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testPasswordChange() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean();
        final AtomicBoolean keychain = new AtomicBoolean();
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }

            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                prompt.set(true);
                // New password entered
                return new Credentials(username, "b");
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
                keychain.set(true);
                // Old password stored
                return "a";
            }
        }, new DisabledProgressListener());
        final Session session = new NullSession(new Host(new TestProtocol(), "localhost", new Credentials("user", ""))) {
            @Override
            public Void connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
                connected.set(true);
                return null;
            }

            @Override
            public boolean isConnected() {
                return connected.get();
            }

            @Override
            public void login(final LoginCallback l, final CancelCallback cancel) throws BackgroundException {
                if(prompt.get()) {
                    assertEquals("b", host.getCredentials().getPassword());
                    throw new LoginCanceledException();
                }
                if(keychain.get()) {
                    assertFalse(prompt.get());
                    assertEquals("a", host.getCredentials().getPassword());
                    throw new LoginFailureException("f");
                }
            }
        };
        try {
            s.check(session, new DisabledCancelCallback());
        }
        finally {
            assertTrue(keychain.get());
            assertTrue(prompt.get());
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectionWarning() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch", new Credentials(
            "u", "p"
        ));
        final AtomicBoolean warned = new AtomicBoolean(false);
        final Session session = new NullSession(host);
        final LoginConnectionService l = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message,
                             final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
            new DisabledProgressListener());
        try {
            l.connect(session, new DisabledCancelCallback());
            fail();
        }
        catch(LoginCanceledException e) {
            assertTrue(warned.get());
            throw e;
        }
    }
}
