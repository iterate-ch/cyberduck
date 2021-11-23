package ch.cyberduck.core.shared;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
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
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultAttributesFinderFeatureTest extends AbstractSFTPTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        new DefaultAttributesFinderFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final AttributesFinder f = new DefaultAttributesFinderFeature(session);
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path file = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(file, new TransferStatus());
        new SFTPUnixPermissionFeature(session).setUnixPermission(file, new Permission("-rw-rw-rw-"));
        final Attributes attributes = f.find(file);
        assertEquals(0L, attributes.getSize());
        assertNotNull(attributes.getOwner());
        assertEquals(new Permission("-rw-rw-rw-"), attributes.getPermission());
        // Test wrong type
        try {
            f.find(new Path(workdir, file.getName(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SFTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
