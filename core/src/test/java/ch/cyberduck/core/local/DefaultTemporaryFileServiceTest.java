package ch.cyberduck.core.local;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class DefaultTemporaryFileServiceTest {

    @Test
    public void testExists() {
        final DefaultTemporaryFileService service = new DefaultTemporaryFileService();
        {
            final Local f = service.create(new AlphanumericRandomStringService().random());
            assertFalse(f.exists());
            assertTrue(f.getParent().exists());
            assertTrue(f.getParent().getParent().exists());
        }
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            final Local f = service.create(new AlphanumericRandomStringService().random(), file);
            assertFalse(f.exists());
            assertTrue(f.getParent().exists());
            assertTrue(f.getParent().getParent().exists());
        }
    }

    @Test
    public void testCreateFile() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = FileSystems.getDefault().getSeparator();
        final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
        final String attributes = String.valueOf(file.attributes().hashCode());
        assertEquals(String.format("%s%su%sp%s%s%sf", temp, s, s, s, attributes, s), new DefaultTemporaryFileService().create("u", file).getAbsolute());
        file.attributes().setRegion("region");
        assertEquals(String.format("%s%su%sp%s%s%sf", temp, s, s, s, attributes, s), new DefaultTemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testVersion() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = FileSystems.getDefault().getSeparator();
        {
            final Path file = new Path("/p/f", EnumSet.of(Path.Type.file));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            final String attributes = String.valueOf(file.attributes().hashCode());
            assertEquals(String.format("%s%su%sp%s%s%sf", temp, s, s, s, attributes, s), new DefaultTemporaryFileService().create("u", file).getAbsolute());
        }
        {
            final Path file = new Path("/p", EnumSet.of(Path.Type.directory));
            file.attributes().setRegion("region");
            file.attributes().setVersionId("2");
            final String attributes = String.valueOf(file.attributes().hashCode());
            assertEquals(String.format("%s%su%s%s%sp", temp, s, s, attributes, s), new DefaultTemporaryFileService().create("u", file).getAbsolute());
        }
    }

    @Test
    public void testCreateContainer() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String s = FileSystems.getDefault().getSeparator();
        final Path file = new Path("/container", EnumSet.of(Path.Type.directory));
        file.attributes().setRegion("region");
        final String attributes = String.valueOf(file.attributes().hashCode());
        assertEquals(String.format("%s%su%s%s%scontainer", temp, s, s, attributes, s), new DefaultTemporaryFileService().create("u", file).getAbsolute());
    }

    @Test
    public void testPathTooLong() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor/invidunt/ut/labore/et/dolore/magna/aliquyam/erat/sed/diam/voluptua/At/vero/eos/et/accusam/et/justo/duo/dolores/et/ea/rebum/Stet/clita/kasd/gubergren/no/sea";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);
        final String testPathMD5 = DigestUtils.md5Hex(testPathDirectory);

        final Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        final Local local = new DefaultTemporaryFileService().create("UID", file);
        final String localFile = local.getAbsolute();
        final String attributes = String.valueOf(file.attributes().hashCode());
        assertNotEquals(String.format("%s/%s%s/%s/%s", temp, "UID", testPathDirectory, attributes, testPathFile).replace('/', File.separatorChar), localFile);
        assertEquals(String.format("%s/%s/%s/%s/%s", temp, "UID", testPathMD5, attributes, testPathFile).replace('/', File.separatorChar), localFile);
    }

    @Test
    public void testPathNotTooLong() {
        final String temp = StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), File.separator);
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);
        final String testPathMD5 = DigestUtils.md5Hex(testPathDirectory);

        Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        final Local local = new DefaultTemporaryFileService().create("UID", file);
        final String localFile = local.getAbsolute();
        final String attributes = String.valueOf(file.attributes().hashCode());
        assertEquals(String.format("%s/%s%s/%s/%s", temp, "UID", testPathDirectory, attributes, testPathFile).replace('/', File.separatorChar), localFile);
        assertNotEquals(String.format("%s/%s%s/%s/%s", temp, "UID", testPathMD5, attributes, testPathFile).replace('/', File.separatorChar), localFile);
    }

    @Test
    public void testTemporaryPath() {
        final Path file = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        file.attributes().setDuplicate(true);
        file.attributes().setVersionId("1");
        final Local local = new DefaultTemporaryFileService().create(file);
        final String attributes = String.valueOf(file.attributes().hashCode());
        assertEquals("t.txt", file.getName());
        assertEquals("t.txt", local.getName());
        assertEquals(attributes, local.getParent().getName());
        assertEquals("f2", local.getParent().getParent().getName());
        assertEquals("f1", local.getParent().getParent().getParent().getName());
    }

    @Test
    public void testTemporaryPathCustomPrefix() {
        final Path file = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        file.attributes().setDuplicate(true);
        file.attributes().setVersionId("1");
        final Local local = new DefaultTemporaryFileService().create("u", file);
        assertTrue(local.getParent().exists());
        assertEquals("t.txt", file.getName());
        assertEquals("t.txt", local.getName());
    }
}
