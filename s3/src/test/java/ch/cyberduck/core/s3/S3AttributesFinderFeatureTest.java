package ch.cyberduck.core.s3;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
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
import java.util.UUID;

import static ch.cyberduck.core.s3.S3VersionedObjectListService.KEY_DELETE_MARKER;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3AttributesFinderFeatureTest extends AbstractS3Test {

    @Test
    public void testFindFileUsEast() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertNotEquals(-1L, attributes.getModificationDate());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory, Path.Type.placeholder)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testFindFileEuCentral() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        f.find(test);
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new S3AttributesFinderFeature(session).find(container);
        assertEquals(-1L, attributes.getSize());
        assertNotNull(attributes.getRegion());
        assertEquals(EnumSet.of(Path.Type.directory, Path.Type.volume), container.getType());
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        f.find(test);
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final PathAttributes attributes = new S3AttributesFinderFeature(session).find(test);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertEquals(0L, attributes.getSize());
        assertEquals(Checksum.parse("d41d8cd98f00b204e9800998ecf8427e"), attributes.getChecksum());
        assertNotEquals(-1L, attributes.getModificationDate());
    }

    @Test
    public void testVersioningReadAttributesDeleteMarker() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path testWithVersionId = new S3TouchFeature(session).touch(test, new TransferStatus());
        final PathAttributes attr = new S3AttributesFinderFeature(session).find(testWithVersionId);
        final String versionId = attr.getVersionId();
        assertNotNull(versionId);
        assertFalse(attr.isDuplicate());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session).find(testWithVersionId);
            for(Path version : marker.getVersions()) {
                assertTrue(version.attributes().isDuplicate());
            }
            assertTrue(marker.isDuplicate());
            assertFalse(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertEquals(versionId, marker.getVersionId());
        }
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session).find(test);
            for(Path version : marker.getVersions()) {
                assertTrue(version.attributes().isDuplicate());
            }
            assertTrue(marker.isDuplicate());
            assertTrue(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertNotEquals(versionId, marker.getVersionId());
        }
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session, true).find(testWithVersionId);
            for(Path version : marker.getVersions()) {
                assertTrue(version.attributes().isDuplicate());
            }
            assertTrue(marker.isDuplicate());
            assertFalse(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertEquals(versionId, marker.getVersionId());
        }
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session, true).find(test);
            for(Path version : marker.getVersions()) {
                assertTrue(version.attributes().isDuplicate());
            }
            assertTrue(marker.isDuplicate());
            assertTrue(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertNotEquals(versionId, marker.getVersionId());
        }
    }

    @Test
    public void testPreviousVersionReferences() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new S3TouchFeature(session).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String versionId = new S3AttributesFinderFeature(session).find(test).getVersionId();
        assertEquals(test.attributes().getVersionId(), versionId);
        final byte[] content = RandomUtils.nextBytes(512);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new S3AttributesFinderFeature(session, true).find(test).getVersions().isEmpty());
        final Path update = new Path(bucket, test.getName(), test.getType(),
            new PathAttributes().withVersionId(out.getStatus().getServiceMetadata("version-id").toString()));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, true).find(update);
        final AttributedList<Path> versions = attributes.getVersions();
        assertFalse(versions.isEmpty());
        assertEquals(test, versions.get(0));
        for(Path version : versions) {
            assertTrue(version.attributes().isDuplicate());
        }
        assertFalse(attributes.isDuplicate());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadTildeInKey() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path file = new Path(container, String.format("%s~", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(file, new TransferStatus());
        new S3AttributesFinderFeature(session).find(file);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadAtSignInKey() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path file = new Path(container, String.format("%s@", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(file, new TransferStatus());
        new S3AttributesFinderFeature(session).find(file);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
