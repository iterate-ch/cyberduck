package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class DAVAttributesFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(UUID.randomUUID().toString() + ".txt", Path.FILE_TYPE);
        final DAVAttributesFeature f = new DAVAttributesFeature(session);
        f.find(test);
    }

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path("/trunk/LICENSE.txt", Path.FILE_TYPE);
        final DAVAttributesFeature f = new DAVAttributesFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(923, attributes.getSize());
        assertNotNull(attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertTrue(attributes.isFile());
    }
}
