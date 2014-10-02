package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class S3AttributesFeatureTest extends AbstractTestCase {

    @Test
    public void testFindFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final String v = UUID.randomUUID().toString();
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum());
        assertNotNull(attributes.getModificationDate());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testFindBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new S3AttributesFeature(session).find(container);
        assertEquals(-1L, attributes.getSize());
        assertEquals(EnumSet.of(Path.Type.directory, Path.Type.volume), container.getType());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AttributesFeature f = new S3AttributesFeature(session);
        f.find(test);
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, "test", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum());
        assertNotNull(attributes.getModificationDate());
        session.close();
    }

    @Test
    public void testVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final PathAttributes attributes = new PathAttributes();
        // Retrieve latest object version
        attributes.setVersionId("xtgd1iPdpb1L0c87oe.3KVul2rcxRyqh");
        assertEquals("L.FLqQnz7SqF99O4QELsdknKY4Kwj2DU", new S3AttributesFeature(session).find(
                new Path("/versioning.test.cyberduck.ch/test", EnumSet.of(AbstractPath.Type.file))).getVersionId());
        session.close();
    }
}
