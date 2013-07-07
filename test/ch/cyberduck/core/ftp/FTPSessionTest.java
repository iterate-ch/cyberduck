package ch.cyberduck.core.ftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FTPSessionTest extends AbstractTestCase {

    @Test
    public void testFallbackDataConnection() throws Exception {
        final Host host = new Host(Protocol.FTP, "mirror.switch.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setFTPConnectMode(FTPConnectMode.PORT);

        final AtomicInteger count = new AtomicInteger();

        final FTPSession session = new FTPSession(host) {
            protected int timeout() {
                return 2000;
            }

            @Override
            protected <T> T fallback(final DataConnectionAction<T> action) throws ConnectionCanceledException, IOException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());


        final Path path = new Path("/pub/debian/README.html", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final FTPSession.DataConnectionAction<Void> action = new FTPSession.DataConnectionAction<Void>() {
            @Override
            public Void run() throws IOException {
                try {
                    assertNotNull(session.read(path, status));
                    assertEquals(1, count.get());
                }
                catch(BackgroundException e) {
                    fail();
                }
                return null;
            }
        };
        session.data(path, action);
    }

    @Test
    public void testConnectAnonymous() throws Exception {
        final Host host = new Host(Protocol.FTP, "mirror.switch.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        assertEquals(Session.State.closed, session.getState());
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertEquals(Session.State.open, session.getState());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertEquals(Session.State.closed, session.getState());
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test(expected = NotfoundException.class)
    public void testNotfound() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        session.list(new Path("/notfound", Path.DIRECTORY_TYPE));
    }


    @Test
    public void testMountFallbackNotfound() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        host.setDefaultPath("/notfound");
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertEquals("/", session.mount().getAbsolute());
    }

    @Test
    public void testConnectionTlsUpgrade() throws Exception {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host) {
            @Override
            public void login(final PasswordStore keychain, final LoginController login) throws BackgroundException {
                assertEquals(Session.State.open, this.getState());
                super.login(keychain, login);
                assertEquals(Protocol.FTP_TLS, host.getProtocol());
            }

            protected boolean isTLSSupported() throws BackgroundException {
                final boolean s = super.isTLSSupported();
                assertTrue(s);
                return true;
            }
        };
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertEquals(Protocol.FTP, host.getProtocol());
        final AtomicBoolean warned = new AtomicBoolean();
        LoginService l = new LoginService(new DisabledLoginController() {
            @Override
            public void warn(final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                warned.set(true);
                // Cancel to switch
                throw new LoginCanceledException();
            }
        }, new DisabledPasswordStore());
        l.login(session, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        });
        assertEquals(Protocol.FTP_TLS, host.getProtocol());
        assertTrue(warned.get());
    }

    @Test
    public void testUnixPermissionFeature() {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final Session session = new FTPSession(host);
        assertNotNull(session.getFeature(UnixPermission.class, null));
    }

    @Test
    public void testFeatures() throws Exception {
        final Session session = new FTPSession(new Host("h"));
        assertNotNull(session.getFeature(UnixPermission.class, null));
        assertNotNull(session.getFeature(Timestamp.class, null));
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
    }

    @Test
    public void testTransfer() throws Exception {
        Path p = new NullPath("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        status.setLength(432768L);
        new FTPSession(new Host("t")).transfer(p, new NullInputStream(status.getLength()), new NullOutputStream(),
                new StreamListener() {
                    long sent;
                    long received;

                    @Override
                    public void bytesSent(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        sent += bytes;
                        assertTrue(sent == received);
                    }

                    @Override
                    public void bytesReceived(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        received += bytes;
                        assertTrue(received > sent);
                    }
                }, -1, status);
        assertTrue(status.isComplete());
        assertTrue(status.getCurrent() == status.getLength());
    }

    @Test
    public void testTransferInterrupt() throws Exception {
        final Path p = new NullPath("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final CyclicBarrier lock = new CyclicBarrier(2);
        final CyclicBarrier exit = new CyclicBarrier(2);
        status.setLength(432768L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new FTPSession(new Host("t")).transfer(p, new NullInputStream(status.getLength()), new NullOutputStream(),
                            new StreamListener() {
                                @Override
                                public void bytesSent(long bytes) {
                                    //
                                }

                                @Override
                                public void bytesReceived(long bytes) {
                                    try {
                                        lock.await();
                                        exit.await();
                                    }
                                    catch(InterruptedException e) {
                                        fail(e.getMessage());
                                    }
                                    catch(BrokenBarrierException e) {
                                        fail(e.getMessage());
                                    }
                                }
                            }, -1, status);
                }
                catch(IOException e) {
                    fail();
                }
                catch(BackgroundException e) {
                    assertTrue(e instanceof ConnectionCanceledException);
                }
            }
        }).start();
        lock.await();
        status.setCanceled();
        exit.await();
        assertFalse(status.isComplete());
        assertTrue(status.isCanceled());
        assertEquals(32768L, status.getCurrent());
    }
}
