package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.UnixPermission;

import org.junit.Test;

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
    public void testUrl() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final SFTPSession session = new SFTPSession(host);
        assertEquals("sftp://u@test.cyberduck.ch/my/documentroot/f", session.toURL(new SFTPPath("/my/documentroot/f", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testHttpUrl() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final SFTPSession session = new SFTPSession(host);
        assertEquals("http://test.cyberduck.ch/f", session.toHttpURL(new SFTPPath("/my/documentroot/f", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testUnixPermissionFeature() {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        final Session session = new SFTPSession(host);
        assertNotNull(session.getFeature(UnixPermission.class, null));
    }
}
