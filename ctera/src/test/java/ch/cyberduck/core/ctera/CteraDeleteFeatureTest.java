package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVLockFeature;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.DELETEPERMISSION;
import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.READPERMISSION;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraDeleteFeatureTest extends AbstractCteraTest {

    @Test
    public void testDeleteFile() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraTouchFeature(session).touch(test, new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(test));
        new CteraDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DAVFindFeature(session).find(test));
    }

    @Test(expected = RetriableAccessDeniedException.class)
    public void testDeleteFileWithLock() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraTouchFeature(session).touch(test, new TransferStatus());
        String lock = null;
        try {
            lock = new DAVLockFeature(session).lock(test);
        }
        catch(InteroperabilityException e) {
            // Not supported
        }
        assertTrue(new DAVFindFeature(session).find(test));
        new CteraDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus().withLockId(lock)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DAVFindFeature(session).find(test));
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new CteraDirectoryFeature(session).mkdir(test, new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(test));
        new CteraTouchFeature(session).touch(new Path(test, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new CteraDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DAVFindFeature(session).find(test));
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new CteraDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testPreflightFileMissingCustomProps() throws BackgroundException {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        file.setAttributes(file.attributes().withAcl(Acl.EMPTY));
        new CteraDeleteFeature(session).preflight(file);
    }

    @Test
    public void testPreflightFileAccessDeniedCustomProps() throws BackgroundException {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser(), READPERMISSION)));
        assertThrows(AccessDeniedException.class, () -> new CteraDeleteFeature(session).preflight(file));
    }

    @Test
    public void testPreflightFileAccessGrantedCustomProps() throws BackgroundException {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        file.setAttributes(file.attributes().withAcl(new Acl(new Acl.CanonicalUser(), DELETEPERMISSION)));
        new CteraDeleteFeature(session).preflight(file);
        // assert no fail
    }
}
