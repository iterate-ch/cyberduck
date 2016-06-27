package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WorkspaceRevealServiceTest {

    @Test
    public void testReveal() throws Exception {
        assertTrue(new WorkspaceRevealService().reveal(new Local(System.getProperty("java.io.tmpdir"))));
    }
}
