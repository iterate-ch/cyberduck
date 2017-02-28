package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
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
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.HTTP, "proxy.local", 6666);
                    }
                });
        s.check(session, PathCache.empty(), new DisabledCancelCallback());
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
            public boolean verify(final String hostname, final int port, final PublicKey key) throws ConnectionCanceledException {
                assertEquals(Session.State.opening, session.getState());
                return true;
            }
        }, new DisabledPasswordStore(),
                new DisabledProgressListener()
        );
        try {
            s.check(session, PathCache.empty(), new DisabledCancelCallback());
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
            }, PathCache.empty(), new DisabledCancelCallback());
            fail();
        }
        catch(LoginCanceledException e) {

        }
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testNoHostname() throws Exception {
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener());
        s.check(new NullSession(new Host(new TestProtocol(), "")), PathCache.empty(), new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testPasswordChange() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean();
        final AtomicBoolean keychain = new AtomicBoolean();
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService s = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                //
            }

            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                // New password entered
                credentials.setPassword("b");
                prompt.set(true);
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore() {
            @Override
            public String find(final Host host) {
                keychain.set(true);
                // Old password stored
                return "a";
            }
        }, new DisabledProgressListener());
        final Session session = new NullSession(new Host(new TestProtocol(), "localhost", new Credentials("user", ""))) {
            @Override
            public Void connect(final HostKeyCallback key) throws BackgroundException {
                connected.set(true);
                return null;
            }

            @Override
            public boolean isConnected() {
                return connected.get();
            }

            @Override
            public void login(final HostPasswordStore p, final LoginCallback l, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
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
            s.check(session, PathCache.empty(), new DisabledCancelCallback());
        }
        finally {
            assertTrue(keychain.get());
            assertTrue(prompt.get());
        }
    }
}
