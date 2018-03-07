package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceTrashFeatureTest {

    @Test
    public void testTrash() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        new WorkspaceTrashFeature().trash(l);
        assertFalse(l.exists());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testTrashNotfound() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertFalse(l.exists());
        new WorkspaceTrashFeature().trash(l);
    }

    @Test
    public void testTrashRepeated() throws Exception {
        final WorkspaceTrashFeature f = new WorkspaceTrashFeature();
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        f.trash(l);
        assertFalse(l.exists());
    }
}
