package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class TemporaryFileServiceTest extends AbstractTestCase {

    @Test
    public void testCreateFile() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        assertEquals(PathNormalizer.normalize(temp + "/u/p/f"),
                new TemporaryFileService().create("u", new Path("/p/f", EnumSet.of(Path.Type.file))).getAbsolute());
        final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
        file.attributes().setRegion("region");
        assertEquals(PathNormalizer.normalize(temp + "/u/p/f"),
                new TemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testVersion() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(PathNormalizer.normalize(temp + "/u/p/2/f"),
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
        {
            final Path file = new Path("/p", EnumSet.of(Path.Type.directory));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(PathNormalizer.normalize(temp + "/u/region2/p"),
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
    }

    @Test
    public void testCreateContainer() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        final Path file = new Path("/container", EnumSet.of(Path.Type.directory));
        file.attributes().setRegion("region");
        assertEquals(PathNormalizer.normalize(temp + "/u/region/container"),
                new TemporaryFileService().create("u", file).getAbsolute());
        file.attributes().setVersionId("2");
        assertEquals(PathNormalizer.normalize(temp + "/u/region2/container"),
                new TemporaryFileService().create("u", file).getAbsolute());
    }
}
