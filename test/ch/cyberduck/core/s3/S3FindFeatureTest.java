package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.features.Find;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3FindFeatureTest extends AbstractTestCase {

    @Test
    public void testFindUnknownBucket() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(UUID.randomUUID().toString(), Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testFindBucket() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
        assertTrue(new S3FindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new S3FindFeature(new S3Session(new Host("h"))).find(new Path("/", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testCacheNotFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = AttributedList.emptyList();
        list.attributes().addHidden(new Path("/g/gd", Path.FILE_TYPE));
        cache.put(new Path("/g", Path.DIRECTORY_TYPE).getReference(), list);
        final Find finder = new S3FindFeature(new S3Session(new Host("t")) {
            @Override
            public S3Session.RequestEntityRestStorageService getClient() {
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
        final Find finder = new S3FindFeature(new S3Session(new Host("t")) {
            @Override
            public S3Session.RequestEntityRestStorageService getClient() {
                fail();
                return null;
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/gd", Path.FILE_TYPE)));
    }

    @Test
    public void testVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final PathAttributes attributes = new PathAttributes(Path.FILE_TYPE);
        final Cache cache = new Cache();
        assertTrue(new S3FindFeature(session).withCache(cache).find(new Path("/versioning.test.cyberduck.ch/test", attributes)));
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", attributes).getReference()));
        attributes.setVersionId("xtgd1iPdpb1L0c87oe.3KVul2rcxRyqh");
        assertTrue(new S3FindFeature(session).withCache(cache).find(new Path("/versioning.test.cyberduck.ch/test", attributes)));
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", attributes).getReference()));
        attributes.setVersionId(null);
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", attributes).getReference()));
        session.close();
    }
}
