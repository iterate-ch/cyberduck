package ch.cyberduck.core.ftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;
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
    public void testFallbackDataConnectionSocketTimeout() throws Exception {
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
            protected <T> T fallback(final DataConnectionAction<T> action) throws IOException, FTPInvalidListException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());

        final Path path = new Path("/pub/debian/README.html", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final DataConnectionAction<Void> action = new DataConnectionAction<Void>() {
            @Override
            public Void execute() throws IOException {
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
    public void testFallbackDataConnection500Error() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        host.setFTPConnectMode(FTPConnectMode.PORT);

        final AtomicInteger count = new AtomicInteger();

        // Expect failure from server
        // 220 (vsFTPd 2.2.2)
        // PORT 192,168,1,38,241,18
        // 550 Permission denied.

        final FTPSession session = new FTPSession(host) {
            protected int timeout() {
                return 2000;
            }

            @Override
            protected <T> T fallback(final DataConnectionAction<T> action) throws IOException, FTPInvalidListException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());

        final Path path = new Path(session.home(), "test", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final DataConnectionAction<Void> action = new DataConnectionAction<Void>() {
            @Override
            public Void execute() throws IOException {
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
        assertFalse(session.isSecured());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount(new DisabledListProgressListener()));
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
//        assertFalse(session.isSecured());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertTrue(session.isSecured());
        final Path path = session.mount(new DisabledListProgressListener());
        assertNotNull(path);
        assertEquals(path, session.workdir());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = BackgroundException.class)
    public void testWorkdir() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        session.workdir();
    }

    @Test
    public void testMakeDirectory() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test, null);
        assertTrue(session.exists(test));
        session.getFeature(Delete.class, new DisabledLoginController()).delete(Collections.singletonList(test));
        assertFalse(session.exists(test));
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.getFeature(Touch.class, new DisabledLoginController()).touch(test);
        assertTrue(session.exists(test));
        session.getFeature(Delete.class, new DisabledLoginController()).delete(Collections.singletonList(test));
        assertFalse(session.exists(test));
        session.close();
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
        session.list(new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE), new DisabledListProgressListener());
    }


    @Test
    public void testMountFallbackNotfound() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        host.setDefaultPath(UUID.randomUUID().toString());
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertEquals("/", session.mount(new DisabledListProgressListener()).getAbsolute());
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
        KeychainLoginService l = new KeychainLoginService(new DisabledLoginController() {
            @Override
            public void warn(final Protocol protocol, final String title, final String message, final String continueButton,
                             final String disconnectButton, final String preference) throws LoginCanceledException {
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
    public void testFeatures() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
        assertNull(session.getFeature(UnixPermission.class, null));
        assertNull(session.getFeature(Timestamp.class, null));
        session.open(new DefaultHostKeyController());
        assertNotNull(session.getFeature(UnixPermission.class, null));
        assertNotNull(session.getFeature(Timestamp.class, null));
        session.close();
    }

    @Test
    public void testTransfer() throws Exception {
        Path p = new Path("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        status.setLength(432768L);
        new FTPSession(new Host("t")).transfer(new NullInputStream(status.getLength()), new NullOutputStream(),
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
        final Path p = new Path("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final CyclicBarrier lock = new CyclicBarrier(2);
        final CyclicBarrier exit = new CyclicBarrier(2);
        status.setLength(432768L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new FTPSession(new Host("t")).transfer(new NullInputStream(status.getLength()), new NullOutputStream(),
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

    @Test
    public void testCloseFailure() throws Exception {
        final Host host = new Host(Protocol.FTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final BackgroundException failure = new BackgroundException("f", new FTPException(500, "f"));
        final FTPSession session = new FTPSession(host) {
            @Override
            protected void logout() throws BackgroundException {
                throw failure;
            }
        };
        session.open(new DefaultHostKeyController());
        assertEquals(Session.State.open, session.getState());
        try {
            session.close();
            fail();
        }
        catch(BackgroundException e) {
            assertEquals(failure, e);
        }
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path test = new Path(session.mount(new DisabledListProgressListener()), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final OutputStream out = session.write(test, status);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        assertTrue(session.exists(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        session.delete(test, new DisabledLoginController());
    }

    @Test
    public void testCopy() throws Exception {

    }
}
