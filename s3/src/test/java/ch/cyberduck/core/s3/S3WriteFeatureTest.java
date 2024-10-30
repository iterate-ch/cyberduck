package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3WriteFeatureTest extends AbstractS3Test {

    @Test
    public void testWritePublicReadCannedAcl() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        status.setAcl(Acl.CANNED_PUBLIC_READ);
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        assertTrue(new S3AccessControlListFeature(session)
                .getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))));
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteCustomTimestamp() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withModified(1630305150672L).withCreated(1695159781972L);
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, acl).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.withAttributes(new S3AttributesAdapter(session.getHost()).toAttributes(out.getStatus()));
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(1630305150000L, attributes.getModificationDate());
        assertEquals(1695159781000L, attributes.getCreationDate());
        assertEquals(1630305150000L, new S3ObjectListService(session, acl, true).list(container,
                new DisabledListProgressListener()).find(new DefaultPathPredicate(test)).attributes().getModificationDate());
        assertEquals(1630305150000L, new S3VersionedObjectListService(session, acl, 50, true).list(container,
                new DisabledListProgressListener()).find(new DefaultPathPredicate(test)).attributes().getModificationDate());
        assertNotEquals(1630305150000L, new S3ObjectListService(session, acl, false).list(container,
                new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes().getModificationDate());
        assertNotEquals(1630305150000L, new S3VersionedObjectListService(session, acl, 50, false).list(container,
                new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes().getModificationDate());
        final Path moved = new S3MoveFeature(session, acl).move(test, new Path(container,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(1630305150000L, new S3AttributesFinderFeature(session, acl).find(moved).getModificationDate());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(moved), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS4Signature() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        try {
            feature.write(file, status, new DisabledConnectionCallback());
        }
        catch(InteroperabilityException e) {
            assertEquals("A header you provided implies functionality that is not implemented. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testWriteAWS2Signature() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 AWS2 Signature Version (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final PathAttributes attr = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(file);
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(content.length, attr.getSize());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteAWS4Signature() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final PathAttributes attr = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(file);
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(content.length, attr.getSize());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteVersionedBucket() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(status.getResponse().getVersionId());
        final PathAttributes attr = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(file);
        assertEquals(status.getResponse().getVersionId(), attr.getVersionId());
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(content.length, attr.getSize());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
