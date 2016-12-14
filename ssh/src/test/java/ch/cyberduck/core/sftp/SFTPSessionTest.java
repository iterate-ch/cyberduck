package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostKeyVerifier;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
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
import net.schmizz.sshj.transport.mac.MAC;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPSessionTest {

    @Test
    public void testLoginPassword() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertFalse(session.isConnected());
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
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
            configuration.setMACFactories(Collections.singletonList(mac));
            final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
            assertTrue(client.isConnected());
            client.close();
        }
    }

    @Test
    public void testAES256CTRCipher() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        final DefaultConfig configuration = new DefaultConfig();
        configuration.setCipherFactories(Collections.singletonList(new AES256CTR.Factory()));
        final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
        assertTrue(client.isConnected());
        client.close();
    }

    @Test
    public void testECDHNistPKeyExchange() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        final DefaultConfig configuration = new DefaultConfig();
        configuration.setKeyExchangeFactories(Collections.singletonList(new ECDHNistP.Factory256()));
        final SSHClient client = session.connect(new DisabledHostKeyCallback(), configuration);
        assertTrue(client.isConnected());
        client.close();
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
            public void prompt(Host bookmark, Credentials credentials,
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
            login.connect(session, PathCache.empty());
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
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        new SFTPHomeDirectoryService(session).find();
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
            });
        }
        catch(Exception e) {
            assertTrue(verify.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectNoValidCredentials() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                assertEquals("Login failed", title);
                assertEquals("Login test.cyberduck.ch with username and password. Please contact your web hosting service provider for assistance.", reason);
                credentials.setUsername("u");
                change.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            login.connect(session, PathCache.empty());
        }
        catch(LoginCanceledException e) {
            assertTrue(change.get());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testValidateNoValidCredentials() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Host bookmark, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                assertEquals("Login test.cyberduck.ch", title);
                assertEquals("Login test.cyberduck.ch with username and password. No login credentials could be found in the Keychain.", reason);
                credentials.setUsername("u");
                change.set(true);
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            login.check(session, PathCache.empty());
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
        final Session session = new SFTPSession(host);
        final AtomicBoolean change = new AtomicBoolean();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Local select(final Local identity) throws LoginCanceledException {
                try {
                    return new NullLocal("k");
                }
                catch(LocalAccessDeniedException e) {
                    fail();
                    throw new LoginCanceledException(e);
                }
            }

            @Override
            public void prompt(Host bookmark, Credentials credentials,
                               String title, String reason, LoginOptions options)
                    throws LoginCanceledException {
                if(change.get()) {
                    assertEquals("Change of username or service not allowed: (u1,ssh-connection) -> (jenkins,ssh-connection). Please contact your web hosting service provider for assistance.", reason);
                }
                else {
                    assertEquals("Login failed", title);
//                    assertEquals("Too many authentication failures for u1. Please contact your web hosting service provider for assistance.", reason);
//                    assertEquals("Exhausted available authentication methods. Please contact your web hosting service provider for assistance.", reason);
                    credentials.setUsername(System.getProperties().getProperty("sftp.user"));
                    credentials.setPassword(System.getProperties().getProperty("sftp.password"));
                    change.set(true);
                }
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        login.connect(session, PathCache.empty());
        assertTrue(change.get());
        session.close();
    }

    @Ignore
    @Test(expected = ConnectionRefusedException.class)
    public void testConnectHttpProxy() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host,
                new ProxySocketFactory(host.getProtocol(), new DefaultTrustManagerHostnameCallback(host),
                        new DefaultSocketConfigurator(), new ProxyFinder() {
                    @Override
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                    }
                })
        );
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            c.connect(session, PathCache.empty());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Invalid response HTTP/1.1 403 Forbidden from HTTP proxy localhost. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", e.getDetail());
            throw e;
        }
        assertTrue(session.isConnected());
        session.close();
    }

    @Test
    public void testHostKeySave() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials("u1", "p1"));
        final Session session = new SFTPSession(host);
        final Local f = new Local("test/ch/cyberduck/core/sftp", "known_hosts");
        final AtomicReference<String> fingerprint = new AtomicReference<String>();
        try {
            assertNotNull(session.open(new OpenSSHHostKeyVerifier(f) {
                @Override
                public boolean verify(final String hostname, final int port, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    fingerprint.set(new SSHFingerprintGenerator().fingerprint(key));
                    return super.verify(hostname, port, key);
                }

                @Override
                protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    this.allow(hostname, key, true);
                    return true;
                }

                @Override
                protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    fail();
                    return false;
                }
            }));
            session.close();
            assertNotNull(session.open(new OpenSSHHostKeyVerifier(f) {
                @Override
                public boolean verify(final String hostname, final int port, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    assertEquals(fingerprint.get(), new SSHFingerprintGenerator().fingerprint(key));
                    return super.verify(hostname, port, key);
                }

                @Override
                protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException, ChecksumException {
                    return false;
                }
            }));
            session.close();
        }
        finally {
            f.delete();
        }
    }
}
