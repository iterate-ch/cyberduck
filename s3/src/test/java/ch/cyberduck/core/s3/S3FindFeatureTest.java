package ch.cyberduck.core.s3;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3FindFeatureTest extends AbstractS3Test {

    @Test
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
    }

    @Test
    public void testFindNotFoundVirtualHost() throws Exception {
        final Path container = Home.root();
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertFalse(new S3FindFeature(virtualhost, new S3AccessControlListFeature(virtualhost)).find(test));
    }

    @Test
    public void testFindUnknownBucket() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(Home.root()));
        assertTrue(new S3FindFeature(virtualhost, new S3AccessControlListFeature(session)).find(Home.root()));
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        assertTrue(new S3FindFeature(session, acl).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new S3TouchFeature(session, acl).touch(
                new S3WriteFeature(session, new S3AccessControlListFeature(session)), new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertFalse(new S3FindFeature(session, acl).find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(new S3ObjectListService(session, acl).list(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener()).contains(test));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertFalse(new S3FindFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(session, cache).find(directory));
        assertTrue(cache.isCached(directory.getParent()));
        assertFalse(new S3FindFeature(session, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }

    @Test
    public void testFindCommonPrefixWithVirtualHost() throws Exception {
        final Path container = Home.root();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        assertTrue(new S3FindFeature(virtualhost, acl).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new S3TouchFeature(virtualhost, acl).touch(
                new S3WriteFeature(virtualhost, new S3AccessControlListFeature(virtualhost)), new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        assertFalse(new S3FindFeature(virtualhost, acl).find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(virtualhost, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(virtualhost, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(new S3ObjectListService(virtualhost, acl).list(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener()).contains(test));
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(virtualhost, acl).find(test));
        assertFalse(new S3FindFeature(virtualhost, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(virtualhost, cache).find(directory));
        assertTrue(cache.isCached(directory.getParent()));
        assertFalse(new S3FindFeature(virtualhost, acl).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }
}
