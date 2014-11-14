package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;

import org.junit.Test;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.cipher.Cipher;
import net.schmizz.sshj.transport.kex.KeyExchange;
import net.schmizz.sshj.transport.mac.MAC;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SFTPSessionTest extends AbstractTestCase {

    @Test
    public void testLoginPassword() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertFalse(session.isConnected());
        assertFalse(session.isSecured());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertFalse(session.isSecured());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(session.isSecured());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testAllHMAC() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        for(net.schmizz.sshj.common.Factory.Named<MAC> mac : new DefaultConfig().getMACFactories()) {
            final DefaultConfig configuration = new DefaultConfig();
            configuration.setMACFactories(Arrays.asList(mac));
            final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
            assertTrue(client.isConnected());
            client.authPassword(properties.getProperty("sftp.user"), properties.getProperty("sftp.password"));
            client.close();
        }
    }

    @Test
    public void testAllCiphers() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        for(net.schmizz.sshj.common.Factory.Named<Cipher> cipher : new DefaultConfig().getCipherFactories()) {
            final DefaultConfig configuration = new DefaultConfig();
            configuration.setCipherFactories(Arrays.asList(cipher));
            final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
            assertTrue(client.isConnected());
            client.authPassword(properties.getProperty("sftp.user"), properties.getProperty("sftp.password"));
            client.close();
        }
    }

    @Test
    public void testAllKeyExchange() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        for(net.schmizz.sshj.common.Factory.Named<KeyExchange> exchange : new DefaultConfig().getKeyExchangeFactories()) {
            final DefaultConfig configuration = new DefaultConfig();
            configuration.setKeyExchangeFactories(Arrays.asList(exchange));
            final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
            assertTrue(client.isConnected());
            client.authPassword(properties.getProperty("sftp.user"), properties.getProperty("sftp.password"));
            client.close();
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailureToomanyauthenticationfailures() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                "jenkins", "p"
        ));
        final SFTPSession session = new SFTPSession(host);
        final AtomicBoolean fail = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                assertEquals("Login failed", title);
//                assertEquals("Too many authentication failures for jenkins. Please contact your web hosting service provider for assistance.", reason);
                fail.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            login.connect(session, Cache.<Path>empty());
        }
        catch(LoginCanceledException e) {
            assertTrue(fail.get());
            throw e;
        }
    }

    @Test(expected = BackgroundException.class)
    public void testWorkdir() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        session.workdir();
    }

    @Test
    public void testFeatures() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        assertNotNull(session.getFeature(Compress.class));
        assertNotNull(session.getFeature(UnixPermission.class));
        assertNotNull(session.getFeature(Timestamp.class));
        assertNotNull(session.getFeature(Touch.class));
        assertNotNull(session.getFeature(Symlink.class));
        assertNotNull(session.getFeature(Command.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testConnectHostKeyDenied() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        final AtomicBoolean verify = new AtomicBoolean();
        try {
            session.open(new HostKeyCallback() {
                @Override
                public boolean verify(String hostname, int port, PublicKey key) throws ConnectionCanceledException {
                    verify.set(true);
                    throw new ConnectionCanceledException();
                }
            }, new DisabledTranscriptListener());
        }
        catch(Exception e) {
            assertTrue(verify.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testNoValidCredentials() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                assertEquals("Login", title);
                assertEquals("Login test.cyberduck.ch with username and password. No login credentials could be found in the Keychain.", reason);
                credentials.setUsername("u");
                change.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            login.connect(session, Cache.<Path>empty());
        }
        catch(LoginCanceledException e) {
            assertTrue(change.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testUsernameChange() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials("anonymous", null));
        final Session session = new SFTPSession(host);
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Local select(final Local identity) throws LoginCanceledException {
                return new NullLocal("k");
            }

            @Override
            public void prompt(Protocol protocol, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                if(change.get()) {
                    assertEquals("Change of username or service not allowed: (anonymous,ssh-connection) -> (u2,ssh-connection). Please contact your web hosting service provider for assistance.", reason);
                    throw new LoginCanceledException();
                }
                else {
                    assertEquals("Login failed", title);
                    assertEquals("Exhausted available authentication methods. Please contact your web hosting service provider for assistance.", reason);
                    credentials.setUsername("u2");
                    change.set(true);
                }
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            login.connect(session, Cache.<Path>empty());
        }
        catch(LoginCanceledException e) {
            assertTrue(change.get());
            throw e;
        }
    }
}
