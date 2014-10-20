package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.features.Find;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path test = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testFindBucket() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new S3FindFeature(new S3Session(new Host("h"))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testCacheNotFound() throws Exception {
        final Cache<Path> cache = new Cache<Path>();
        final AttributedList<Path> list = AttributedList.emptyList();
        list.attributes().addHidden(new Path("/g/gd", EnumSet.of(Path.Type.file)));
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)).getReference(), list);
        final Find finder = new S3FindFeature(new S3Session(new Host("t")) {
            @Override
            public S3Session.RequestEntityRestStorageService getClient() {
                fail();
                return null;
            }
        }).withCache(cache);
        assertFalse(finder.find(new Path("/g/gd", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testCacheFound() throws Exception {
        final Cache<Path> cache = new Cache<Path>();
        final AttributedList<Path> list = new AttributedList<Path>(Collections.singletonList(new Path("/g/gd", EnumSet.of(Path.Type.file))));
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)).getReference(), list);
        final Find finder = new S3FindFeature(new S3Session(new Host("t")) {
            @Override
            public S3Session.RequestEntityRestStorageService getClient() {
                fail();
                return null;
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/gd", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final PathAttributes attributes = new PathAttributes();
        final Cache<Path> cache = new Cache<Path>();
        assertTrue(new S3FindFeature(session).withCache(cache).find(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), attributes)));
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), attributes).getReference()));
        attributes.setVersionId("xtgd1iPdpb1L0c87oe.3KVul2rcxRyqh");
        assertTrue(new S3FindFeature(session).withCache(cache).find(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), attributes)));
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), attributes).getReference()));
        attributes.setVersionId(null);
        assertNotNull(cache.lookup(new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(Path.Type.file), attributes).getReference()));
        session.close();
    }
}
