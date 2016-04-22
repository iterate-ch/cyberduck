package ch.cyberduck.core.local;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class TemporaryFileServiceTest {

    @Test
    public void testCreateFile() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        final String s = System.getProperty("file.separator");
        assertEquals(String.format("%su%sp%sf", temp, s, s),
                new TemporaryFileService().create("u", new Path(String.format("%sp%sf", s, s), EnumSet.of(Path.Type.file))).getAbsolute());
        final Path file = new Path(String.format("%sp%sf", s, s), EnumSet.of(Path.Type.file));
        file.attributes().setRegion("region");
        assertEquals(String.format("%su%sp%sf", temp, s, s),
                new TemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testVersion() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        final String s = System.getProperty("file.separator");
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(String.format("%su%sp%s2%sf", temp, s, s, s),
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
        {
            final Path file = new Path("/p", EnumSet.of(Path.Type.directory));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(String.format("%su%sregion2%sp", temp, s, s),
                    new TemporaryFileService().create("u", file).getAbsolute());
        }
    }

    @Test
    public void testCreateContainer() throws Exception {
        final String temp = System.getProperty("java.io.tmpdir");
        final String s = System.getProperty("file.separator");
        final Path file = new Path("/container", EnumSet.of(Path.Type.directory));
        file.attributes().setRegion("region");
        assertEquals(String.format("%su%sregion%scontainer", temp, s, s),
                new TemporaryFileService().create("u", file).getAbsolute());
        file.attributes().setVersionId("2");
        assertEquals(String.format("%su%sregion2%scontainer", temp, s, s),
                new TemporaryFileService().create("u", file).getAbsolute());
    }
}
