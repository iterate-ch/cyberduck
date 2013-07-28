package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SFTPSessionTest extends AbstractTestCase {

    @Test
    public void testLoginPassword() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertFalse(session.isConnected());
        assertFalse(session.isSecured());
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertFalse(session.isSecured());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertTrue(session.isSecured());
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = BackgroundException.class)
    public void testWorkdir() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        session.workdir();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginCancel() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
    }

    @Test
    public void testFeatures() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch");
        final Session session = new SFTPSession(host);
        assertNotNull(session.getFeature(Compress.class, null));
        assertNotNull(session.getFeature(UnixPermission.class, null));
        assertNotNull(session.getFeature(Timestamp.class, null));
        assertNotNull(session.getFeature(Touch.class, null));
        assertNotNull(session.getFeature(Symlink.class, null));
        assertNotNull(session.getFeature(Command.class, null));
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
    }

    @Test
    public void testMakeDirectory() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test, null);
        assertTrue(session.exists(test));
        session.delete(test, new DisabledLoginController());
        session.close();
    }

    @Test
    public void testReadWrite() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
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
        final byte[] buffer = new byte[content.length];
        final InputStream in = session.read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        IOUtils.closeQuietly(in);
        assertArrayEquals(content, buffer);
        session.delete(test, new DisabledLoginController());
        session.close();
    }
}
