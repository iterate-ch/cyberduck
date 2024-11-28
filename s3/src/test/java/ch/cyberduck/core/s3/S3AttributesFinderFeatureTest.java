package ch.cyberduck.core.s3;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.s3.S3VersionedObjectListService.KEY_DELETE_MARKER;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3AttributesFinderFeatureTest extends AbstractS3Test {

    @Test
    public void testFindRoot() throws Exception {
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session));
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFindFileUsEast() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session, acl);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Checksum.parse(attributes.getETag()).hash);
        assertNotEquals(-1L, attributes.getModificationDate());
        assertEquals(test.attributes(), attributes);
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory, Path.Type.placeholder)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testFindFileEuCentral() throws Exception {
        final Path test = new Path(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session));
        f.find(test);
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(container);
        assertNotSame(PathAttributes.EMPTY, attributes);
        assertEquals(-1L, attributes.getSize());
        assertNotNull(attributes.getRegion());
        assertEquals(EnumSet.of(Path.Type.directory, Path.Type.volume), container.getType());
        final PathAttributes listAttributes = new S3BucketListService(session, new S3LocationFeature.S3Region(session.getHost().getRegion())).list(
                container.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(container)).attributes();
        assertNotSame(PathAttributes.EMPTY, listAttributes);
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session));
        f.find(test);
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final PathAttributes attributes = new S3AttributesFinderFeature(session, acl).find(test);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertEquals(0L, attributes.getSize());
        assertEquals(Checksum.parse("d41d8cd98f00b204e9800998ecf8427e"), attributes.getChecksum());
        assertNotEquals(-1L, attributes.getModificationDate());
    }

    @Test
    public void testVersioningReadAttributesDeleteMarker() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path f = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path testWithVersionId = new S3TouchFeature(session, acl).touch(f, new TransferStatus());
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(f);
        final String versionId = attr.getVersionId();
        assertNotNull(versionId);
        assertEquals(testWithVersionId.attributes().getVersionId(), versionId);
        assertEquals(testWithVersionId.attributes(), attr);
        assertFalse(attr.isDuplicate());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(new Path(testWithVersionId).withAttributes(PathAttributes.EMPTY)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session, acl).find(testWithVersionId);
            assertTrue(marker.isDuplicate());
            assertFalse(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertEquals(versionId, marker.getVersionId());
        }
        {
            final PathAttributes marker = new S3AttributesFinderFeature(session, acl).find(testWithVersionId);
            assertTrue(marker.isDuplicate());
            assertFalse(marker.getCustom().containsKey(KEY_DELETE_MARKER));
            assertNotNull(marker.getVersionId());
            assertEquals(versionId, marker.getVersionId());
        }
        {
            try {
                new S3AttributesFinderFeature(session, acl).find(new Path(testWithVersionId).withAttributes(PathAttributes.EMPTY));
                fail();
            }
            catch(NotfoundException e) {
                // Delete marker
            }
        }
        {
            try {
                new S3AttributesFinderFeature(session, acl).find(new Path(testWithVersionId).withAttributes(PathAttributes.EMPTY));
                fail();
            }
            catch(NotfoundException e) {
                // Delete marker
            }
        }
    }

    @Test
    public void testReadTildeInKey() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path file = new S3TouchFeature(session, acl).touch(
                new Path(container, String.format("%s~", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        new S3AttributesFinderFeature(session, acl).find(file);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadAtSignInKey() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path file = new S3TouchFeature(session, acl).touch(
                new Path(container, String.format("%s@", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        new S3AttributesFinderFeature(session, acl).find(file);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWhitespaceInKey() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(directory, String.format("%s %s", new AlphanumericRandomStringService(4).random(), new AlphanumericRandomStringService(4).random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(file);
        new S3DefaultDeleteFeature(session, acl).delete(Arrays.asList(directory, file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        assertTrue(new S3FindFeature(session, acl).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new S3TouchFeature(session, acl).touch(
                new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new S3AttributesFinderFeature(session, acl).find(test));
        assertNotNull(new S3AttributesFinderFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new S3AttributesFinderFeature(session, acl).find(test);
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            new S3AttributesFinderFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }

    @Test
    public void testRedirectWithNoLocationHeader() throws Exception {
        final Path container = new Path("profiles.cyberduck.io", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, "S3 (HTTP).cyberduckprofile", EnumSet.of(Path.Type.file));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session));
        f.find(test);
        assertTrue(session.getClient().getRegionEndpointCache().containsRegionForBucketName("profiles.cyberduck.io"));
        assertEquals("eu-west-1", session.getClient().getRegionEndpointCache().getRegionForBucketName("profiles.cyberduck.io"));
    }

    @Test
    public void testDeleted() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(PathAttributes.EMPTY, new S3AttributesFinderFeature(session, acl).find(test));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertThrows(NotfoundException.class, () -> new S3AttributesFinderFeature(session, acl).find(test));
    }

    @Test
    public void testDeletedWithMarker() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(PathAttributes.EMPTY, new S3AttributesFinderFeature(session, acl).find(test));
        // Add delete marker
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(new Path(test).withAttributes(PathAttributes.EMPTY)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(new Path(test)));
        assertFalse(new S3AttributesFinderFeature(session, acl).find(test).getCustom().containsKey(KEY_DELETE_MARKER));
        assertFalse(new S3FindFeature(session, acl).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        // Test reading delete marker itself
        final Path marker = new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertTrue(marker.attributes().isDuplicate());
        assertTrue(marker.attributes().getCustom().containsKey(KEY_DELETE_MARKER));
        assertTrue(new S3AttributesFinderFeature(session, acl).find(marker).getCustom().containsKey(KEY_DELETE_MARKER));
        assertTrue(new S3FindFeature(session, acl).find(marker));
    }

    @Test
    public void testCloudFront() throws Exception {
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(cloudfront, new S3AccessControlListFeature(cloudfront));
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
        final String name = new AlphanumericRandomStringService().random();
        final TransferStatus status = new TransferStatus();
        status.setAcl(Acl.CANNED_PUBLIC_READ);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path file = new S3TouchFeature(session, acl).touch(
                new Path(new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                        name, EnumSet.of(Path.Type.file)), status);
        final PathAttributes attributes = f.find(new Path(file.getName(), EnumSet.of(Path.Type.file)));
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertNotEquals(-1L, attributes.getModificationDate());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testVirtualHostStyle() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(virtualhost, acl);
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
        final String name = new AlphanumericRandomStringService().random();
        final TransferStatus status = new TransferStatus();
        final Path file = new S3TouchFeature(virtualhost, new S3AccessControlListFeature(virtualhost)).touch(
                new Path(name, EnumSet.of(Path.Type.file)), status);
        final PathAttributes attributes = f.find(new Path(file.getName(), EnumSet.of(Path.Type.file)));
        assertEquals(0L, attributes.getSize());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum().hash);
        assertNotEquals(-1L, attributes.getModificationDate());
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDetermineRegionVirtualHostStyle() throws Exception {
        assertEquals("eu-central-1", virtualhost.getClient().getRegionEndpointCache().getRegionForBucketName(""));
        assertEquals("eu-central-1", virtualhost.getClient().getRegionEndpointCache().getRegionForBucketName("test-eu-central-1-cyberduck"));
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(virtualhost, new S3AccessControlListFeature(virtualhost));
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // No region is cached and must be determined although HEAD request will not allow S3 to return correct region to use in AWS4 signature
        f.find(new Path(file.getName(), EnumSet.of(Path.Type.file)));
    }
}
