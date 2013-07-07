package ch.cyberduck.core.cloud;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CloudPathTest extends AbstractTestCase {

    @Test
    public void testIsContainer() throws Exception {
        assertFalse(new CloudPath("/", Path.VOLUME_TYPE).isContainer());
        assertTrue(new CloudPath("/t", Path.VOLUME_TYPE).isContainer());
        assertTrue(new CloudPath("/t/", Path.VOLUME_TYPE).isContainer());
        assertFalse(new CloudPath("/t/a", Path.VOLUME_TYPE).isContainer());
    }

    @Test
    public void testGetContainerName() throws Exception {
        assertEquals("t", new CloudPath("/t", Path.FILE_TYPE).getContainer().getName());
        assertEquals("t", new CloudPath("/t/a", Path.FILE_TYPE).getContainer().getName());
    }

    @Test
    public void testGetContainer() throws Exception {
        assertEquals("/t", new CloudPath("/t", Path.FILE_TYPE).getContainer().getAbsolute());

    }

    @Test
    public void testGetKey() throws Exception {
        assertEquals("d/f", new CloudPath("/c/d/f", Path.DIRECTORY_TYPE).getKey());
    }
}
