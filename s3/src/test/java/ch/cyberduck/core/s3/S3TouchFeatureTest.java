package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3TouchFeatureTest {

    @Test
    public void testFile() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "h"));
        assertFalse(new S3TouchFeature(session).isSupported(new Path("/", EnumSet.of(Path.Type.volume))));
        assertTrue(new S3TouchFeature(session).isSupported(new Path(new Path("/", EnumSet.of(Path.Type.volume)), "/container", EnumSet.of(Path.Type.volume))));
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test(expected = AccessDeniedException.class)
    public void testFailureWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.encryption.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(Encryption.Algorithm.NONE);
        touch.touch(test, status);
    }

    public void testSuccessWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.encryption.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(S3EncryptionFeature.SSE_AES256);
        touch.touch(test, status);
    }

    @Test
    public void testConnectionReuse() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3TouchFeature service = new S3TouchFeature(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final List<Path> list = new ArrayList<Path>();
        for(int i = 0; i < 200; i++) {
            final String name = String.format("%s-%d", UUID.randomUUID().toString(), i);
            final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
            service.touch(test);
            list.add(test);
        }
        new S3MultipleDeleteFeature(session).delete(list, new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}
