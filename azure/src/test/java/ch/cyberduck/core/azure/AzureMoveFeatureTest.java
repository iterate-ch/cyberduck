package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
public class AzureMoveFeatureTest extends AbstractAzureTest {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes sourceAttr = new AzureAttributesFinderFeature(session).find(test);
        assertTrue(new AzureFindFeature(session).find(test));
        final Path target = new AzureMoveFeature(session).move(test, new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), ConnectionCallback.noop);
        assertFalse(new AzureFindFeature(session).find(test));
        assertTrue(new AzureFindFeature(session).find(target));
        final PathAttributes targetAttr = new AzureAttributesFinderFeature(session).find(target);
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, sourceAttr, targetAttr));
        new AzureDeleteFeature(session).delete(Collections.<Path>singletonList(target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverwriteFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path moved = new AzureMoveFeature(session).move(source, target, new TransferStatus().setExists(true), new Delete.DisabledCallback(), ConnectionCallback.noop);
        assertFalse(new AzureFindFeature(session).find(source));
        assertTrue(new AzureFindFeature(session).find(moved));
        new AzureDeleteFeature(session).delete(Collections.singletonList(moved), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverwriteEmptyDirectory() throws Exception {
        assumeTrue(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(source, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path moved = new AzureMoveFeature(session).move(source, target, new TransferStatus().setExists(true), new Delete.DisabledCallback(), ConnectionCallback.noop);
        assertFalse(new AzureFindFeature(session).find(source));
        assertTrue(new AzureFindFeature(session).find(new Path(moved, file.getName(), EnumSet.of(Path.Type.file))));
        new AzureDeleteFeature(session).delete(Collections.singletonList(moved), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectoryToNonEmptyDirectory() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new AzureDirectoryFeature(session).mkdir(new AzureWriteFeature(session), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        new AzureTouchFeature(session).touch(new AzureWriteFeature(session), new Path(target, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new AzureMoveFeature(session).move(source, target, new TransferStatus().setExists(true), new Delete.DisabledCallback(), ConnectionCallback.noop);
        new AzureDeleteFeature(session).delete(Collections.singletonList(target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMoveSharedAccessSignature() throws Exception {
        final Host host = session.getHost();
        final String token = new AccountSasSignatureValues(OffsetDateTime.now().plusHours(1),
                AccountSasPermission.parse("rwdlacup"), AccountSasService.parse("b"), AccountSasResourceType.parse("sco"))
                .generateSasQueryParameters(new StorageSharedKeyCredential(host.getCredentials().getUsername(), host.getCredentials().getPassword())).encode();
        final AzureSession sas = new AzureSession(new Host(new AzureProtocol(), host.getHostname(), new Credentials(host.getCredentials().getUsername(), null, token)));
        new LoginConnectionService(LoginCallback.noop, HostKeyCallback.noop,
                new TestPasswordStore(), ProgressListener.noop).check(sas, CancelCallback.noop);
        try {
            final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
            final Path source = new AzureTouchFeature(sas).touch(new AzureWriteFeature(sas), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
            final Path target = new AzureMoveFeature(sas).move(source, new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), ConnectionCallback.noop);
            assertFalse(new AzureFindFeature(sas).find(source));
            assertTrue(new AzureFindFeature(sas).find(target));
            new AzureDeleteFeature(sas).delete(Collections.singletonList(target), LoginCallback.noop, new Delete.DisabledCallback());
        }
        finally {
            sas.close();
        }
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new AzureMoveFeature(session).isSupported(c, Optional.of(new Path("/d", EnumSet.of(Path.Type.directory)))));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new AzureMoveFeature(session).isSupported(cf, Optional.of(new Path("/c/f2", EnumSet.of(Path.Type.directory)))));
    }
}
