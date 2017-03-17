package ch.cyberduck.core.local;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class DefaultTemporaryFileServiceTest {

    @Test
    public void testExists() throws Exception {
        final Local f = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        assertFalse(f.exists());
        assertFalse(f.getParent().exists());
        assertTrue(f.getParent().getParent().exists());
    }

    @Test
    public void testCreateFile() throws Exception {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = System.getProperty("file.separator");
        assertEquals(String.format("%s%su%sp%sf", temp, s, s, s),
                new DefaultTemporaryFileService().create("u", new Path("/p/f", EnumSet.of(Path.Type.file))).getAbsolute());
        final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
        file.attributes().setRegion("region");
        assertEquals(String.format("%s%su%sp%sf", temp, s, s, s),
                new DefaultTemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testVersion() throws Exception {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = System.getProperty("file.separator");
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(String.format("%s%su%sp%s2%sf", temp, s, s, s, s),
                    new DefaultTemporaryFileService().create("u", file).getAbsolute());
        }
        {
            final Path file = new Path("/p", EnumSet.of(Path.Type.directory));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            assertEquals(String.format("%s%su%sregion2%sp", temp, s, s, s),
                    new DefaultTemporaryFileService().create("u", file).getAbsolute());
        }
    }

    @Test
    public void testCreateContainer() throws Exception {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = System.getProperty("file.separator");
        final Path file = new Path("/container", EnumSet.of(Path.Type.directory));
        file.attributes().setRegion("region");
        assertEquals(String.format("%s%su%sregion%scontainer", temp, s, s, s),
                new DefaultTemporaryFileService().create("u", file).getAbsolute());
        file.attributes().setVersionId("2");
        assertEquals(String.format("%s%su%sregion2%scontainer", temp, s, s, s),
                new DefaultTemporaryFileService().create("u", file).getAbsolute());
    }
}
