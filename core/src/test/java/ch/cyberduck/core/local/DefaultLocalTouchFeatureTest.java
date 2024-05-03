package ch.cyberduck.core.local;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultLocalTouchFeatureTest {

    @Test
    public void testTouch() throws Exception {
        Local parent = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        Local l = new Local(parent, UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        // Test create missing parent directory
        f.touch(l);
        assertTrue(parent.exists());
        assertTrue(l.exists());
        assertThrows(LocalAccessDeniedException.class, () -> f.touch(l));
        l.delete();
        parent.delete();
    }

    @Test
    public void testFailure() {
        Local l = new Local("/" + UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        try {
            f.touch(l);
        }
        catch(AccessDeniedException e) {
            final String s = l.getName();
            assertEquals("Cannot create " + PreferencesFactory.get().getProperty("local.delimiter") + s + ". Please verify disk permissions.", e.getDetail());
            assertEquals("Access denied", e.getMessage());
        }
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testFolderExists() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        new DefaultLocalDirectoryFeature().mkdir(l);
        assertTrue(l.exists());
        f.touch(l);
        assertTrue(l.exists());
    }
}
