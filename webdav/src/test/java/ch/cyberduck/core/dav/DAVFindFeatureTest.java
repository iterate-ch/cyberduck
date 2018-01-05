package ch.cyberduck.core.dav;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DAVFindFeatureTest {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(new DAVFindFeature(session).find(new DefaultHomeFinderService(session).find()));
        assertFalse(new DAVFindFeature(session).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory))
        ));
        assertFalse(new DAVFindFeature(session).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ));
        session.close();
    }


    @Test
    public void testFindAnonymous() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(new DAVFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertFalse(new DAVFindFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.file))));
        assertFalse(new DAVFindFeature(session).find(new Path("/trunk/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new DAVFindFeature(new DAVSession(new Host(new DAVProtocol(), "h"))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
