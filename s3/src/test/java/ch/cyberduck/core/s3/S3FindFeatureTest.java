package ch.cyberduck.core.s3;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3FindFeatureTest extends AbstractS3Test {

    @Test
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3FindFeature f = new S3FindFeature(session, new S3AccessControlListFeature(session));
        assertFalse(f.find(test));
    }

    @Test
    public void testFindUnknownBucket() throws Exception {
        final Path test = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new S3FindFeature(new S3Session(new Host(new S3Protocol())), new S3AccessControlListFeature(session)).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(new S3ObjectListService(session, new S3AccessControlListFeature(session)).list(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener()).contains(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(cache, new S3FindFeature(session, new S3AccessControlListFeature(session))).find(directory));
        assertFalse(cache.isCached(directory));
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }
}
