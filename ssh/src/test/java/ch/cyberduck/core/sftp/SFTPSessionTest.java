package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Session;
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
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostKeyVerifier;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.PublicKey;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.cipher.AES256CTR;
import net.schmizz.sshj.transport.kex.ECDHNistP;
import net.schmizz.sshj.transport.mac.HMACSHA2256;
import net.schmizz.sshj.transport.mac.HMACSHA2512;
import net.schmizz.sshj.transport.mac.MAC;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPSessionTest extends AbstractSFTPTest {

    @Test
    public void testLoginPassword() {
        assertTrue(session.isConnected());
    }

    @Test
    public void testAllHMAC() throws Exception {
        final DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setMACFactories(
                new HMACSHA2256.Factory(),
                new HMACSHA2512.Factory()
        );
        for(net.schmizz.sshj.common.Factory.Named<MAC> mac : defaultConfig.getMACFactories()) {
            final DefaultConfig configuration = new DefaultConfig();
            configuration.setMACFactories(Collections.singletonList(mac));
            final SSHClient client = session.connect(new DisabledHostKeyCallback(), new DisabledLoginCallback(), configuration);
            assertTrue(client.isConnected());
            client.close();
        }
    }

    @Test
    public void testAES256CTRCipher() throws Exception {
        final DefaultConfig configuration = new DefaultConfig();
        configuration.setCipherFactories(Collections.singletonList(new AES256CTR.Factory()));
        final SSHClient client = session.connect(new DisabledHostKeyCallback(), new DisabledLoginCallback(), configuration);
        assertTrue(client.isConnected());
        client.close();
    }

    @Test
    public void testECDHNistPKeyExchange() throws Exception {
        final DefaultConfig configuration = new DefaultConfig();
        configuration.setKeyExchangeFactories(Collections.singletonList(new ECDHNistP.Factory256()));
        final SSHClient client = session.connect(new DisabledHostKeyCallback(), new DisabledLoginCallback(), configuration);
        assertTrue(client.isConnected());
        client.close();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailureTooManyAuthenticationFailures() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                "jenkins", "p"
        )) {
            @Override
            public String getProperty(final String key) {
                if("ssh.authentication.agent.enable".equals(key)) {
                    return String.valueOf(false);
                }
                return null;
            }
        };
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final AtomicBoolean fail = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username,
                                      String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Login test.cyberduck.ch", title);
                assertEquals("Exhausted available authentication methods. Please contact your web hosting service provider for assistance.", reason);
                fail.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener());
        try {
            login.connect(session, new DisabledCancelCallback());
        }
        catch(LoginCanceledException e) {
            assertTrue(fail.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testWorkdir() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        new SFTPHomeDirectoryService(session).find();
    }

    @Test
    public void testFeatures() {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
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
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final AtomicBoolean verify = new AtomicBoolean();
        try {
            session.open(Proxy.DIRECT, new HostKeyCallback() {
                @Override
                public boolean verify(Host hostname, PublicKey key) throws ConnectionCanceledException {
                    verify.set(true);
                    throw new ConnectionCanceledException();
                }
            }, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(Exception e) {
            assertTrue(verify.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectNoValidCredentials() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials("user", "p")) {
            @Override
            public String getProperty(final String key) {
                if("ssh.authentication.agent.enable".equals(key)) {
                    return String.valueOf(false);
                }
                return null;
            }
        };
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
            new DisabledProgressListener());
        login.connect(session, new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testValidateNoValidCredentials() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username,
                                      String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                assertEquals("Login test.cyberduck.ch", title);
                assertEquals("Login test.cyberduck.ch – SFTP with username and password. Select the private key in PEM or PuTTY format. No login credentials could be found in the Keychain.", reason);
                change.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener());
        try {
            login.check(session, new DisabledCancelCallback());
        }
        catch(LoginCanceledException e) {
            assertTrue(change.get());
            throw e;
        }
    }

    @Test
    @Ignore
    public void testUsernameChangeReconnect() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials("u1", "p1"));
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Local select(final Local identity) throws LoginCanceledException {
                return new NullLocal("k");
            }

            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) {
                if(change.get()) {
                    assertEquals("Change of username or service not allowed: (u1,ssh-connection) -> (jenkins,ssh-connection). Please contact your web hosting service provider for assistance.", reason);
                    return null;
                }
                else {
                    assertEquals("Login failed", title);
//                    assertEquals("Too many authentication failures for u1. Please contact your web hosting service provider for assistance.", reason);
//                    assertEquals("Exhausted available authentication methods. Please contact your web hosting service provider for assistance.", reason);
                    change.set(true);
                    return new Credentials(System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password"));
                }
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener());
        login.connect(session, new DisabledCancelCallback());
        assertTrue(change.get());
    }

    @Test
    public void testHostKeySave() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials("u1", "p1"));
        final Session session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Local f = new Local("test/ch/cyberduck/core/sftp", "known_hosts");
        final AtomicReference<String> fingerprint = new AtomicReference<String>();
        try {
            assertNotNull(session.open(Proxy.DIRECT, new OpenSSHHostKeyVerifier(f) {
                @Override
                public boolean verify(final Host hostname, final PublicKey key) throws BackgroundException {
                    fingerprint.set(new SSHFingerprintGenerator().fingerprint(key));
                    return super.verify(hostname, key);
                }

                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    this.allow(hostname, key, true);
                    return true;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    fail();
                    return false;
                }
            }, new DisabledLoginCallback(), new DisabledCancelCallback()));
            session.close();
            assertNotNull(session.open(Proxy.DIRECT, new OpenSSHHostKeyVerifier(f) {
                @Override
                public boolean verify(final Host hostname, final PublicKey key) throws BackgroundException {
                    assertEquals(fingerprint.get(), new SSHFingerprintGenerator().fingerprint(key));
                    return super.verify(hostname, key);
                }

                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }
            }, new DisabledLoginCallback(), new DisabledCancelCallback()));
            session.close();
        }
        finally {
            f.delete();
        }
    }
}
