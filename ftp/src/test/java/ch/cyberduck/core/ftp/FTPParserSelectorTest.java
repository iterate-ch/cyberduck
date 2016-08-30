package ch.cyberduck.core.ftp;

import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

public class FTPParserSelectorTest {

    @Test
    public void testGetParser() throws Exception {
        assertNotNull(new FTPParserSelector().getParser(null));
    }

    @Test
    public void testGetMVS() throws Exception {
        final CompositeFileEntryParser parser = new FTPParserSelector().getParser("MVS is the operating system of this server. FTP Server is running on z/OS.");
        final String line = "drwxr-xr-x   6 START2   SYS1        8192 Oct 28  2008 ADCD";
        parser.preParse(Arrays.asList("total 66", line));
        assertNotNull(parser.parseFTPEntry(line));
    }
}
