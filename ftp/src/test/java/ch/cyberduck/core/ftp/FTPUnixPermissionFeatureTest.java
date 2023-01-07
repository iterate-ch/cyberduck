package ch.cyberduck.core.ftp;

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ftp.list.FTPListService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class FTPUnixPermissionFeatureTest extends AbstractFTPTest {

    @Test(expected = InteroperabilityException.class)
    public void testSetUnixPermission() throws Exception {
        final FTPWorkdirService workdir = new FTPWorkdirService(session);
        final Path home = workdir.find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(test, new TransferStatus());
        new FTPUnixPermissionFeature(session).setUnixPermission(test, new Permission(666));
        assertEquals("666", new FTPListService(session, null, TimeZone.getDefault()).list(home, new DisabledListProgressListener()).get(test).attributes().getPermission().getMode());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
