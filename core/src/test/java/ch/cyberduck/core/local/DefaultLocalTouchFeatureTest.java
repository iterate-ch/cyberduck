package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultLocalTouchFeatureTest {

    @Test
    public void testTouch() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        f.touch(l);
        assertTrue(l.exists());
        f.touch(l);
        assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testFailure() throws Exception {
        Local l = new Local("/" + UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        try {
            f.touch(l);
        }
        catch(AccessDeniedException e) {
            final String s = l.getName();
            assertEquals("Cannot create /" + s + ". Please verify disk permissions.", e.getDetail());
            assertEquals("Access denied", e.getMessage());
        }
    }

    @Test
    public void testSkipWhenFolderExists() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        l.mkdir();
        assertTrue(l.exists());
        f.touch(l);
        assertTrue(l.exists());
    }
}
