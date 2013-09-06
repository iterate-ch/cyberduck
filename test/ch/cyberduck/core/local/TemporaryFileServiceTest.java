package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class TemporaryFileServiceTest extends AbstractTestCase {

    @Test
    public void testCreate() throws Exception {
        assertEquals(System.getProperty("java.io.tmpdir") + "u/p/f-1",
                new TemporaryFileService().create("u", new Path("/p/f", Path.FILE_TYPE)).getAbsolute());
    }
}
