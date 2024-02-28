// An extension to the UnixFTPEntryParser, making it understand EPLF,
// as described in http://cr.yp.to/ftp/list/eplf.html .

package ch.cyberduck.core.ftp.parser;

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EPLFEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("UNIX");
    }

    @Test
    public void testStandardBinls() {
        // Legacy test.
        // Just to make sure we don't break the standard parser.
        FTPFile parsed = parser.parseFTPEntry("drwxrwxr-x   7 root     ftpadmin     1024 Apr 20 16:17 pub");

        assertTrue(parsed.isDirectory());
        assertEquals("pub", parsed.getName());

        assertEquals(1024, parsed.getSize(), 0);

        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue(parsed.isDirectory());
        assertFalse(parsed.isFile());
        assertFalse(parsed.isSymbolicLink());
        assertEquals("root", parsed.getUser());
        assertEquals("ftpadmin", parsed.getGroup());

//        assertEquals("rwxrwxr-x (775)", parsed.getPermission().toString());
    }

    @Test
    public void testReadonlyFile() {
        FTPFile parsed = parser.parseFTPEntry("+m825718503,r,s280,\tdjb.html\r\n");

        assertEquals("djb.html", parsed.getName());
        assertFalse(parsed.isDirectory());

        assertEquals(280, parsed.getSize(), 0);

        long millis = 825718503;
        millis = millis * 1000;
        assertEquals(millis, parsed.getTimestamp().getTimeInMillis());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertFalse(parsed.isDirectory());
        assertTrue(parsed.isFile());
        assertFalse(parsed.isSymbolicLink());
//        assertEquals("owner", "Unknown", parsed.getUser());
//        assertEquals("group", "Unknown", parsed.getGroup());

//        assertEquals("permissions", "r--r--r-- (444)", parsed.attributes.getPermission().toString());
    }

    @Test
    public void testReadonlyDirectory() {
        FTPFile parsed = parser.parseFTPEntry("+m825718503,/,\t514");

        assertEquals("514", parsed.getName());
        assertTrue(parsed.isDirectory());

//        assertEquals("size", -1, parsed.getSize(), 0);

        long millis = 825718503;
        millis = millis * 1000;
        assertEquals(millis, parsed.getTimestamp().getTimeInMillis());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue(parsed.isDirectory());
        assertFalse(parsed.isFile());
        assertFalse(parsed.isSymbolicLink());
//        assertEquals("owner", "Unknown", parsed.getUser());
//        assertEquals("group", "Unknown", parsed.getGroup());

//        assertEquals("permissions", "r-xr-xr-x (555)", parsed.getPermission().toString());
    }

    @Test
    public void testSpecifiedPermissionsOverrideStandardDirPermissions() {
        FTPFile parsed = parser.parseFTPEntry("+up153,/,\t514");
        assertTrue(parsed.isDirectory());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue(parsed.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.getPermission().toString());
    }

    @Test
    public void testSpecifiedPermissionsDoesntRemoveDirTag() {
        FTPFile parsed = parser.parseFTPEntry("+/,up153,\t514");
        assertTrue(parsed.isDirectory());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue(parsed.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.getPermission().toString());
    }

    @Test
    public void testSpecifiedPermissionsOverrideStandardFilePermissions() {
        FTPFile parsed = parser.parseFTPEntry("+up153,r,\tmyfile");
        assertFalse(parsed.isDirectory());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertTrue(parsed.isFile());
    }

    @Test
    public void testHideUnreadableFilesAndDirs() {
        // Missing both 'r' (may be RETRed) and '/' (may be CWDed) fact.
        assertNull(parser.parseFTPEntry("+m825718503,\tuseless"));
    }

    @Test
    public void testEmptyFacts() {
        // The following EPLF entries are all malformed, but we try to ignore the errors.
        long millis = 825718503;
        millis = millis * 1000;
        FTPFile parsed;

        parsed = parser.parseFTPEntry("+,m825718503,r,s280,\tdjb.html\r\n");
        assertEquals("djb.html", parsed.getName());
        assertFalse(parsed.isDirectory());
        assertEquals(280, parsed.getSize(), 0);
        assertEquals(millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());

        parsed = parser.parseFTPEntry("+m825718503,,r,s280,\tdjb.html\r\n");
        assertEquals("djb.html", parsed.getName());
        assertFalse(parsed.isDirectory());
        assertEquals(280, parsed.getSize(), 0);
        assertEquals(millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());

        parsed = parser.parseFTPEntry("+m825718503,r,s280,,\tdjb.html\r\n");
        assertEquals("djb.html", parsed.getName());
        assertFalse(parsed.isDirectory());
        assertEquals(280, parsed.getSize(), 0);
        assertEquals(millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());
    }

    @Test
    public void testNoFacts() {
        // We know nothing but the name of the mystery file, which essentially means a RETR or CWD is futile.
        assertNull(parser.parseFTPEntry("+\tMysteryFile"));
    }

    @Test
    public void testAFewNotVeryInterestingFiles() {
        assertNull(parser.parseFTPEntry("+/,\t."));
        assertNull(parser.parseFTPEntry("+/,\t.."));
        assertNull(parser.parseFTPEntry("+r,\t\r\n"));
        assertNull(parser.parseFTPEntry("+\t\r\n"));
    }

    @Test
    public void testMissingNameSeparator() {
        assertNull(parser.parseFTPEntry("+r,s1234,  notabinfront\r\n"));
    }
}
