package ch.cyberduck.core.shared;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPTouchFeature;
import ch.cyberduck.core.sftp.SFTPUnixPermissionFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CachingAttributesFinderFeatureTest extends AbstractSFTPTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final PathCache cache = new PathCache(1);
        final CachingAttributesFinderFeature f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        f.find(test);
        // Test cache
        new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(test);
    }

    @Test
    public void testAttributes() throws Exception {
        final PathCache cache = new PathCache(1);
        final AttributesFinder f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path file = new SFTPTouchFeature(session).touch(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new SFTPUnixPermissionFeature(session).setUnixPermission(file, new Permission("-rw-rw-rw-"));
        final Attributes lookup = f.find(file);
        assertEquals(0L, lookup.getSize());
        assertNotNull(lookup.getOwner());
        assertEquals(new Permission("-rw-rw-rw-"), lookup.getPermission());
        // Test cache
        assertSame(lookup, new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(file));
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(workdir, file.getName(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        cache.invalidate(workdir);
        final PathAttributes newAttr = new PathAttributes();
        assertSame(newAttr, new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                return newAttr;
            }
        }).find(file));
        new SFTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
