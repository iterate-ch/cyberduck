package ch.cyberduck.core.s3;

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
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
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random() + ".txt", EnumSet.of(Path.Type.file));
        assertNull(new S3TouchFeature(session).touch(test, new TransferStatus()).attributes().getVersionId());
        assertTrue(new S3FindFeature(session).find(test));
        final Map<String, String> metadata = new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testTouchVersioning() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path file = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String version1 = new S3TouchFeature(session).touch(file, new TransferStatus()).attributes().getVersionId();
        final String version2 = new S3TouchFeature(session).touch(file, new TransferStatus()).attributes().getVersionId();
        assertTrue(new S3FindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version1))));
        assertTrue(new DefaultFindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version2))));
        assertTrue(new S3FindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version1))));
        assertTrue(new S3FindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version2))));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        // Versioned files are not deleted but with delete marker added
        assertTrue(new DefaultFindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version1))));
        assertTrue(new DefaultFindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version2))));
        assertTrue((new S3FindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version1)))));
        assertTrue((new S3FindFeature(session).find(new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(version2)))));
        session.close();
    }

    @Test(expected = AccessDeniedException.class)
    public void testFailureWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(Encryption.Algorithm.NONE);
        touch.touch(test, status);
    }

    @Test
    public void testSuccessWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Encryption.class) {
                    return (T) new S3EncryptionFeature(this) {
                        @Override
                        public Algorithm getDefault(final Path file) {
                            return S3EncryptionFeature.SSE_AES256;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final TransferStatus status = new TransferStatus();
        touch.touch(test, status);
    }
}
