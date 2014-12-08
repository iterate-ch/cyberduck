package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class WorkspaceRevealServiceTest extends AbstractTestCase {

    @Test
    public void testReveal() throws Exception {
        assertTrue(new WorkspaceRevealService().reveal(new FinderLocal(System.getProperty("java.io.tmpdir"))));
    }
}
