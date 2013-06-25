package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.ui.cocoa.AlertHostKeyController;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class SFTPSessionTest extends AbstractTestCase {

    @Test
    @Ignore
    public void testConnect() throws Exception {
        AlertHostKeyController.register();
//        HostKeyControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new PreferencesHostKeyVerifier());

        final Host host = new Host(Protocol.SFTP, "localhost", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
    }

    @Test
    public void testUrl() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final SFTPSession session = new SFTPSession(host);
        assertEquals("sftp://u@test.cyberduck.ch/my/documentroot/f", session.toURL(new SFTPPath(session, "/my/documentroot/f", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testHttpUrl() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final SFTPSession session = new SFTPSession(host);
        assertEquals("http://test.cyberduck.ch/f", session.toHttpURL(new SFTPPath(session, "/my/documentroot/f", Path.DIRECTORY_TYPE)));
    }
}
