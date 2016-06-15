package ch.cyberduck.core;

import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MACOSX_PORT_165Test {

    @Test
    public void testListFiles() throws IOException {
        final String u = UUID.randomUUID().toString();
        final File file = File.createTempFile(u, "-ä");
        for(File f : file.getParentFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.startsWith(u);
            }
        })) {
            assertEquals(f.getName(), file.getName());
        }
    }
}