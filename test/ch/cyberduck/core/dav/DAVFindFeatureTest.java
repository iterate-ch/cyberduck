package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class DAVFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertTrue(new DAVFindFeature(session).find(new DefaultHomeFinderService(session).find()));
        assertFalse(new DAVFindFeature(session).find(
                new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE)
        ));
        assertFalse(new DAVFindFeature(session).find(
                new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE)
        ));
        session.close();
    }


    @Test
    public void testFindAnonymous() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
        assertFalse(new DAVFindFeature(session).find(new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk/LICENSE.txt", Path.FILE_TYPE)));
        assertFalse(new DAVFindFeature(session).find(new Path("/trunk/" + UUID.randomUUID().toString(), Path.FILE_TYPE)));
        session.close();
    }
}
