package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class WorkspaceRevealServiceTest extends AbstractTestCase {

    @Test
    public void testReveal() throws Exception {
        assertTrue(new WorkspaceRevealService().reveal(new FinderLocal(System.getProperty("java.io.tmpdir"))));
    }
}
