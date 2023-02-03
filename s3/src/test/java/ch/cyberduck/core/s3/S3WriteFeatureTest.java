package ch.cyberduck.core.s3;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

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
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteCustomTimestamp() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withTimestamp(1630305150672L);
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, acl).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.withAttributes(new S3AttributesAdapter().toAttributes(out.getStatus()));
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(1630305150672L, new S3AttributesFinderFeature(session, acl).find(test).getModificationDate());
        assertEquals(1630305150672L, new S3ObjectListService(session, acl, true).list(container,
                new DisabledListProgressListener()).find(new DefaultPathPredicate(test)).attributes().getModificationDate());
        assertEquals(1630305150672L, new S3VersionedObjectListService(session, acl, 1, true).list(container,
                new DisabledListProgressListener()).find(new DefaultPathPredicate(test)).attributes().getModificationDate());
        assertNotEquals(1630305150672L, new S3ObjectListService(session, acl, false).list(container,
                new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes().getModificationDate());
        assertNotEquals(1630305150672L, new S3VersionedObjectListService(session, acl, 1, false).list(container,
                new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes().getModificationDate());
        final Path moved = new S3MoveFeature(session, acl).move(test, new Path(container,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(1630305150672L, new S3AttributesFinderFeature(session, acl).find(moved).getModificationDate());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(moved), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testAppendBelowLimit() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertFalse(append.append);
    }

    @Test
    public void testSize() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L).withRemote(new PathAttributes().withSize(3L)));
        assertFalse(append.append);
        assertEquals(0L, append.size, 0L);
    }

    @Test
    public void testAppendNoMultipartFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3WriteFeature(session, new S3AccessControlListFeature(session)).append(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(Long.MAX_VALUE)).append);
        assertEquals(Write.override, new S3WriteFeature(session, new S3AccessControlListFeature(session)).append(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(Long.MAX_VALUE)));
        assertEquals(Write.override, new S3WriteFeature(session, new S3AccessControlListFeature(session)).append(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L)));
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS2SignatureFailure() throws Exception {
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        feature.write(file, status, new DisabledConnectionCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS4Signature() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
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
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
