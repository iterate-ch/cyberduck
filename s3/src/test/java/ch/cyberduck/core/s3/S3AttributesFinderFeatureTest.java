package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3AttributesFinderFeatureTest {

    @Test
    public void testFindFileUsEast() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

            @Override
            public <T> T _getFeature(final Class<T> type) {
                if(type == Versioning.class) {
                    return null;
                }
                return super._getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertNotNull(attributes.getModificationDate());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory, Path.Type.placeholder)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testFindFileEuCentral() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

            @Override
            public <T> T _getFeature(final Class<T> type) {
                if(type == Versioning.class) {
                    return null;
                }
                return super._getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        session.close();
    }

    @Test
    public void testFindBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new S3AttributesFinderFeature(session).find(container);
        assertEquals(-1L, attributes.getSize());
        assertEquals(EnumSet.of(Path.Type.directory, Path.Type.volume), container.getType());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

        };
        session.open(new DisabledHostKeyCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        f.find(test);
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(test, null, new TransferStatus());
        final PathAttributes attributes = new S3AttributesFinderFeature(session).find(test);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertEquals(0L, attributes.getSize());
        assertEquals(Checksum.parse("d41d8cd98f00b204e9800998ecf8427e"), attributes.getChecksum());
        assertNotNull(attributes.getModificationDate());
        session.close();
    }

    @Test
    public void testVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        final PathAttributes attributes = new PathAttributes();
        // Retrieve latest object version
        attributes.setVersionId("a.wvRLBNdY1MncqxF5Jt.hPn3NejhheK");
        assertEquals("a.wvRLBNdY1MncqxF5Jt.hPn3NejhheK", new S3AttributesFinderFeature(session).find(
                new Path("/versioning-test-us-east-1-cyberduck/test", EnumSet.of(AbstractPath.Type.file))).getVersionId());
        session.close();
    }

    @Test
    public void testReadTildeInKey() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path file = new Path(container, String.format("%s~", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(file, new TransferStatus());
        new S3AttributesFinderFeature(session).find(file);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadAtSignInKey() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path file = new Path(container, String.format("%s@", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(file, new TransferStatus());
        new S3AttributesFinderFeature(session).find(file);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
