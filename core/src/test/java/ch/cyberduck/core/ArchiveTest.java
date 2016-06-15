package ch.cyberduck.core;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class ArchiveTest {

    @Test
    public void testForName() throws Exception {
        assertEquals(Archive.TAR, Archive.forName("tar"));
        assertEquals(Archive.TARGZ, Archive.forName("tar.gz"));
        assertEquals(Archive.ZIP, Archive.forName("zip"));
    }

    @Test
    public void testEscape() throws Exception {
        Archive a = Archive.TAR;
        assertEquals("file\\ name", a.escape("file name"));
        assertEquals("file\\(name", a.escape("file(name"));
        assertEquals("\\$filename", a.escape("$filename"));
    }

    @Test
    public void testCommand() throws FactoryException {
        assertEquals("cd /a; zip -qr /a/b.zip b",
                Archive.ZIP.getCompressCommand(new Path("/a", EnumSet.of(Path.Type.directory)),
                        Collections.singletonList(new Path("/a/b", EnumSet.of(Path.Type.file)))));
        assertEquals("cd /a; zip -qr /a/b\\ c.zip b\\ c",
                Archive.ZIP.getCompressCommand(new Path("/a", EnumSet.of(Path.Type.directory)),
                        Collections.singletonList(new Path("/a/b c", EnumSet.of(Path.Type.file)))));
        assertEquals("cd /a; tar -cpPf /a/b.tar b",
                Archive.TAR.getCompressCommand(new Path("/a", EnumSet.of(Path.Type.directory)),
                        Collections.singletonList(new Path("/a/b", EnumSet.of(Path.Type.file)))));
    }
}