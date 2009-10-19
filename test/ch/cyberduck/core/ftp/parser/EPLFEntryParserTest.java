// An extension to the UnixFTPEntryParser, making it understand EPLF,
// as described in http://cr.yp.to/ftp/list/eplf.html .

package ch.cyberduck.core.ftp.parser;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.ftp.FTPParserFactory;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

public class EPLFEntryParserTest extends AbstractTestCase {

    static {
        org.apache.log4j.BasicConfigurator.configure();
    }

    public EPLFEntryParserTest(String name) {
        super(name);
    }

    private FTPFileEntryParser parser;

    @Override
    public void setUp() {
        super.setUp();
        this.parser = new FTPParserFactory().createFileEntryParser("UNIX");
    }

    public void testStandardBinls() {
        // Legacy test.
        // Just to make sure we don't break the standard parser.
        FTPFile parsed = parser.parseFTPEntry("drwxrwxr-x   7 root     ftpadmin     1024 Apr 20 16:17 pub");

        assertTrue("is dir", parsed.isDirectory());
        assertEquals("pub", parsed.getName());

        assertEquals(1024, parsed.getSize(), 0);

        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue("attr is dir", parsed.isDirectory());
        assertFalse("attr is file", parsed.isFile());
        assertFalse("attr is link", parsed.isSymbolicLink());
        assertEquals("root", parsed.getUser());
        assertEquals("ftpadmin", parsed.getGroup());

//        assertEquals("rwxrwxr-x (775)", parsed.getPermission().toString());
    }

    public void testReadonlyFile() {
        FTPFile parsed = parser.parseFTPEntry("+m825718503,r,s280,\tdjb.html\r\n");

        assertEquals("name", "djb.html", parsed.getName());
        assertFalse("is dir", parsed.isDirectory());

        assertEquals(280, parsed.getSize(), 0);

        long millis = 825718503;
        millis = millis * 1000;
        assertEquals("timestamp", millis, parsed.getTimestamp().getTimeInMillis());
        assertEquals("type", FTPFile.FILE_TYPE, parsed.getType());
        assertFalse("attr is dir", parsed.isDirectory());
        assertTrue("attr is file", parsed.isFile());
        assertFalse("attr is link", parsed.isSymbolicLink());
//        assertEquals("owner", "Unknown", parsed.getUser());
//        assertEquals("group", "Unknown", parsed.getGroup());

//        assertEquals("permissions", "r--r--r-- (444)", parsed.attributes.getPermission().toString());
    }

    public void testReadonlyDirectory() {
        FTPFile parsed = parser.parseFTPEntry("+m825718503,/,\t514");

        assertEquals("name", "514", parsed.getName());
        assertTrue("is dir", parsed.isDirectory());

//        assertEquals("size", -1, parsed.getSize(), 0);

        long millis = 825718503;
        millis = millis * 1000;
        assertEquals("timestamp", millis, parsed.getTimestamp().getTimeInMillis());
        assertEquals("type", FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue("attr is dir", parsed.isDirectory());
        assertFalse("attr is file", parsed.isFile());
        assertFalse("attr is link", parsed.isSymbolicLink());
//        assertEquals("owner", "Unknown", parsed.getUser());
//        assertEquals("group", "Unknown", parsed.getGroup());

//        assertEquals("permissions", "r-xr-xr-x (555)", parsed.getPermission().toString());
    }

    public void testSpecifiedPermissionsOverrideStandardDirPermissions() {
        FTPFile parsed = parser.parseFTPEntry("+up153,/,\t514");
        assertTrue("is dir", parsed.isDirectory());
        assertEquals("type", FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue("attr is dir", parsed.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.getPermission().toString());
    }

    public void testSpecifiedPermissionsDoesntRemoveDirTag() {
        FTPFile parsed = parser.parseFTPEntry("+/,up153,\t514");
        assertTrue("is dir", parsed.isDirectory());
        assertEquals("type", FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertTrue("attr is dir", parsed.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.getPermission().toString());
    }

    public void testSpecifiedPermissionsOverrideStandardFilePermissions() {
        FTPFile parsed = parser.parseFTPEntry("+up153,r,\tmyfile");
        assertFalse("is dir", parsed.isDirectory());
        assertEquals("type", FTPFile.FILE_TYPE, parsed.getType());
        assertTrue("attr is file", parsed.isFile());
//        assertEquals("--xr-x-wx (153)", parsed.getPermission().toString());
    }

    public void testHideUnreadableFilesAndDirs() {
        // Missing both 'r' (may be RETRed) and '/' (may be CWDed) fact.
        assertNull(parser.parseFTPEntry("+m825718503,\tuseless"));
    }

    public void testEmptyFacts() {
        // The following EPLF entries are all malformed, but we try to ignore the errors.
        long millis = 825718503;
        millis = millis * 1000;
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry("+,m825718503,r,s280,\tdjb.html\r\n");
        assertEquals("name", "djb.html", parsed.getName());
        assertFalse("is dir", parsed.isDirectory());
        assertEquals("size", 280, parsed.getSize(), 0);
        assertEquals("timestamp", millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());

        parsed = parser.parseFTPEntry("+m825718503,,r,s280,\tdjb.html\r\n");
        assertEquals("X name", "djb.html", parsed.getName());
        assertFalse("X is dir", parsed.isDirectory());
        assertEquals("X size", 280, parsed.getSize(), 0);
        assertEquals("timestamp", millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());

        parsed = parser.parseFTPEntry("+m825718503,r,s280,,\tdjb.html\r\n");
        assertEquals("XX name", "djb.html", parsed.getName());
        assertFalse("XX is dir", parsed.isDirectory());
        assertEquals("XX size", 280, parsed.getSize(), 0);
        assertEquals("timestamp", millis, parsed.getTimestamp().getTimeInMillis());
//        assertEquals("permissions", "--------- (000)", parsed.getPermission().toString());
    }

    public void testNoFacts() {
        // We know nothing but the name of the mystery file, which essentially means a RETR or CWD is futile.
        assertNull(parser.parseFTPEntry("+\tMysteryFile"));
    }

    public void testAFewNotVeryInterestingFiles() {
        assertNull("dot", parser.parseFTPEntry("+/,\t."));
        assertNull("dot dot", parser.parseFTPEntry("+/,\t.."));
        assertNull("empty", parser.parseFTPEntry("+r,\t\r\n"));
        assertNull("really empty", parser.parseFTPEntry("+\t\r\n"));
    }

    public void testMissingNameSeparator() {
        assertNull(parser.parseFTPEntry("+r,s1234,  notabinfront\r\n"));
    }
}
