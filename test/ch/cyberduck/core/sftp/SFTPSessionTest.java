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

import org.junit.Test;

import java.util.Collections;
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
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test));
        session.close();
    }
}
