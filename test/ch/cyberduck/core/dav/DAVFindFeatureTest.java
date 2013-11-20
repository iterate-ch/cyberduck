package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
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
        assertTrue(new DAVFindFeature(session).find(new Path("/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
        assertFalse(new DAVFindFeature(session).find(new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
        assertTrue(new DAVFindFeature(session).find(new Path("/trunk/LICENSE.txt", Path.FILE_TYPE)));
        assertFalse(new DAVFindFeature(session).find(new Path("/trunk/" + UUID.randomUUID().toString(), Path.FILE_TYPE)));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new DAVFindFeature(new DAVSession(new Host("h"))).find(new Path("/", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testCacheNotFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = AttributedList.emptyList();
        list.attributes().addHidden(new Path("/g/gd", Path.FILE_TYPE));
        cache.put(new Path("/g", Path.DIRECTORY_TYPE).getReference(), list);
        final Find finder = new DAVFindFeature(new DAVSession(new Host("t")) {
            @Override
            public DAVClient getClient() {
                fail();
                return null;
            }
        }).withCache(cache);
        assertFalse(finder.find(new Path("/g/gd", Path.FILE_TYPE)));
    }

    @Test
    public void testCacheFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = new AttributedList<Path>(Collections.singletonList(new Path("/g/gd", Path.FILE_TYPE)));
        cache.put(new Path("/g", Path.DIRECTORY_TYPE).getReference(), list);
        final Find finder = new DAVFindFeature(new DAVSession(new Host("t")) {
            @Override
            public DAVClient getClient() {
                fail();
                return null;
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/gd", Path.FILE_TYPE)));
    }
}
