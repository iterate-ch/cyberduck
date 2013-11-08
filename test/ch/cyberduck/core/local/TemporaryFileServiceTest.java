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
    public void testCreateFile() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        assertEquals(temp + "u/p/f",
                new TemporaryFileService().create("u", new Path("/p/f", Path.FILE_TYPE)).getAbsolute());
        final Path file = new Path("/p/f", Path.FILE_TYPE);
        file.attributes().setRegion("region");
        assertEquals(temp + "u/p/f",
                new TemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testVersion() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        {
            final Path file = new Path("/p/f", Path.FILE_TYPE);
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(temp + "u/p/2/f",
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
        {
            final Path file = new Path("/p", Path.DIRECTORY_TYPE);
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(temp + "u/region2/p",
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
    }

    @Test
    public void testCreateContainer() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        final Path file = new Path("/container", Path.DIRECTORY_TYPE);
        file.attributes().setRegion("region");
        assertEquals(temp + "u/region/container",
                new TemporaryFileService().create("u", file).getAbsolute());
        file.attributes().setVersionId("2");
        assertEquals(temp + "u/region2/container",
                new TemporaryFileService().create("u", file).getAbsolute());
    }
}
