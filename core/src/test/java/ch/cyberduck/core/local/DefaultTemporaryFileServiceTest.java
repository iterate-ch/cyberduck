package ch.cyberduck.core.local;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.codec.digest.DigestUtils;
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

    @Test
    public void testPathTooLong() {
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor/invidunt/ut/labore/et/dolore/magna/aliquyam/erat/sed/diam/voluptua/At/vero/eos/et/accusam/et/justo/duo/dolores/et/ea/rebum/Stet/clita/kasd/gubergren/no/sea";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);
        final String testPathMD5 = DigestUtils.md5Hex(testPathDirectory);

        PreferencesFactory.get().setProperty("tmp.dir", "/temp"); // overwrite tmp.dir for better results
        final Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        Local local = new DefaultTemporaryFileService().create("UID", file);
        assertNotEquals(String.format("/%s/%s/%s/2/%s", "temp", "UID", testPathDirectory, testPathFile), local.getAbsolute());
        assertEquals(String.format("/%s/%s/%s/2/%s", "temp", "UID", testPathMD5, testPathFile), local.getAbsolute());
    }

    @Test
    public void testPathNotTooLong() {
        final String testPathDirectory = "/Lorem/ipsum/dolor/sit/amet/consetetur/sadipscing/elitr/sed/diam/nonumy/eirmod/tempor";
        final String testPathFile = "takimata.sanc";
        final String testPath = String.format("%s/%s", testPathDirectory, testPathFile);
        final String testPathMD5 = DigestUtils.md5Hex(testPathDirectory);

        PreferencesFactory.get().setProperty("tmp.dir", "/temp"); // overwrite tmp.dir for better results
        final Path file = new Path(testPath, EnumSet.of(Path.Type.file));
        file.attributes().setVersionId("2");
        Local local = new DefaultTemporaryFileService().create("UID", file);
        assertEquals(String.format("/%s/%s/%s/2/%s", "temp", "UID", testPathDirectory, testPathFile), local.getAbsolute());
        assertNotEquals(String.format("/%s/%s/%s/2/%s", "temp", "UID", testPathMD5, testPathFile), local.getAbsolute());
    }
}
