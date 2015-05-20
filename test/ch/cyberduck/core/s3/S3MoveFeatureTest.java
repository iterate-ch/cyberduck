package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3MoveFeatureTest extends AbstractTestCase {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        final Path renamed = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, false, new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(renamed), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testMoveWithDelimiter() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(placeholder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final Path renamed = new Path(placeholder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, false, new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(renamed), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testSupport() throws Exception {
        assertFalse(new S3MoveFeature(null).isSupported(new Path("/c", EnumSet.of(Path.Type.directory))));
        assertTrue(new S3MoveFeature(null).isSupported(new Path("/c/f", EnumSet.of(Path.Type.directory))));
    }

    @Test(expected = AccessDeniedException.class)
    public void testMoveWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.encryption.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).withEncryption("AES256").touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        final Path renamed = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, false, new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(renamed), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }
}
