package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class WorkspaceTrashFeatureTest extends AbstractTestCase {

    @Test
    public void testTrash() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        new WorkspaceTrashFeature().trash(l);
        assertFalse(l.exists());
    }

    @Test
    public void testTrashRepeated() throws Exception {
        final WorkspaceTrashFeature f = new WorkspaceTrashFeature();
        this.repeat(new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                new DefaultLocalTouchFeature().touch(l);
                assertTrue(l.exists());
                f.trash(l);
                assertFalse(l.exists());
                return l;
            }
        }, 10);
    }
}
