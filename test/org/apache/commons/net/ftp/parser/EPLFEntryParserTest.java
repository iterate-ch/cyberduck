// An extension to the UnixFTPEntryParser, making it understand EPLF,
// as described in http://cr.yp.to/ftp/list/eplf.html .

package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.*;

import java.util.Date;

public class EPLFEntryParserTest extends junit.framework.TestCase {

    static {
		org.apache.log4j.BasicConfigurator.configure();
	}
    
    public EPLFEntryParserTest(String name) {
        super(name);
    }
    
	private FTPFileEntryParser parser;
    private Path parentPath;

    public void setUp() {
		this.parser = new UnixFTPEntryParser();
		Host host = new Host("localhost", 21);
		host.setCredentials("anonymous", "anonymous@example.net");
		Session session = SessionFactory.createSession(host);
		this.parentPath = PathFactory.createPath(session, "/");
    }

    public void testStandardBinls() {
        // Legacy test.
        // Just to make sure we don't break the standard parser.
        Path parsed = parser.parseFTPEntry(parentPath, "drwxrwxr-x   7 root     ftpadmin     1024 Apr 20 16:17 pub");

        assertTrue("is dir", parsed.attributes.isDirectory());
        assertFalse("is root", parsed.isRoot());
        assertEquals("pub", parsed.getName());
        assertEquals("/pub", parsed.getAbsolute());

        assertEquals(1024, parsed.attributes.getSize(), 0);

        assertNotNull("timestamp", parsed.attributes.getTimestamp());
        assertEquals(Path.DIRECTORY_TYPE, parsed.attributes.getType());
        assertTrue("attr is dir", parsed.attributes.isDirectory());
        assertFalse("attr is file", parsed.attributes.isFile());
        assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        assertEquals("root", parsed.attributes.getOwner());
        assertEquals("ftpadmin", parsed.attributes.getGroup());

        assertEquals("rwxrwxr-x (775)", parsed.attributes.getPermission().toString());
    }
    
    public void testNull() {
        // Legacy test.
        // The original parser barfed on null.
        boolean caught = false;
        try { parser.parseFTPEntry(parentPath, null);
        } catch (NullPointerException expected) { caught = true; }
        if (!caught) fail("should throw null pointer exception");
    }

    public void testReadonlyFile() {
        Path parsed = parser.parseFTPEntry(parentPath, "+m825718503,r,s280,\tdjb.html\r\n");

        assertEquals("name", "djb.html", parsed.getName());
        assertEquals("absolute", "/djb.html", parsed.getAbsolute());
        assertFalse("is dir", parsed.attributes.isDirectory());
        assertFalse("is root", parsed.isRoot());
        
        assertEquals(280, parsed.attributes.getSize(), 0);
        
        long millis = 825718503;
        millis = millis * 1000;
        assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());
        assertEquals("type", Path.FILE_TYPE, parsed.attributes.getType());
        assertFalse("attr is dir", parsed.attributes.isDirectory());
        assertTrue("attr is file", parsed.attributes.isFile());
        assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        assertEquals("owner", "Unknown", parsed.attributes.getOwner());
        assertEquals("group", "Unknown", parsed.attributes.getGroup());
        
//        assertEquals("permissions", "r--r--r-- (444)", parsed.attributes.getPermission().toString());
    }
    
    public void testReadonlyDirectory() {
        Path parsed = parser.parseFTPEntry(parentPath, "+m825718503,/,\t514");
        
        assertEquals("name", "514", parsed.getName());
        assertEquals("absolute", "/514", parsed.getAbsolute());
        assertTrue("is dir", parsed.attributes.isDirectory());
        assertFalse("is root", parsed.isRoot());
        
        assertEquals("size", -1, parsed.attributes.getSize(), 0);
        
        long millis = 825718503;
        millis = millis * 1000;
        assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());
        assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        assertTrue("attr is dir", parsed.attributes.isDirectory());
        assertFalse("attr is file", parsed.attributes.isFile());
        assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        assertEquals("owner", "Unknown", parsed.attributes.getOwner());
        assertEquals("group", "Unknown", parsed.attributes.getGroup());
        
//        assertEquals("permissions", "r-xr-xr-x (555)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsOverrideStandardDirPermissions() {
        Path parsed = parser.parseFTPEntry(parentPath, "+up153,/,\t514");
        assertTrue("is dir", parsed.attributes.isDirectory());
        assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        assertTrue("attr is dir", parsed.attributes.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsDoesntRemoveDirTag() {
        Path parsed = parser.parseFTPEntry(parentPath, "+/,up153,\t514");
        assertTrue("is dir", parsed.attributes.isDirectory());
        assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        assertTrue("attr is dir", parsed.attributes.isDirectory());
//        assertEquals("--xr-x-wx (153)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsOverrideStandardFilePermissions() {
        Path parsed = parser.parseFTPEntry(parentPath, "+up153,r,\tmyfile");
        assertFalse("is dir", parsed.attributes.isDirectory());
        assertEquals("type", Path.FILE_TYPE, parsed.attributes.getType());
        assertTrue("attr is file", parsed.attributes.isFile());
//        assertEquals("--xr-x-wx (153)", parsed.attributes.getPermission().toString());
    }
    
    public void testHideUnreadableFilesAndDirs() {
        // Missing both 'r' (may be RETRed) and '/' (may be CWDed) fact.
        assertNull(parser.parseFTPEntry(parentPath, "+m825718503,\tuseless"));
    }

    public void testEmptyFacts() {
        // The following EPLF entries are all malformed, but we try to ignore the errors.
        long millis = 825718503;
        millis = millis * 1000;
        Path parsed = null;
        
        parsed = parser.parseFTPEntry(parentPath, "+,m825718503,r,s280,\tdjb.html\r\n");
        assertEquals("name", "djb.html", parsed.getName());
        assertFalse("is dir", parsed.attributes.isDirectory());
        assertEquals("size", 280, parsed.attributes.getSize(), 0);
        assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());
        assertEquals("permissions", "--------- (000)", parsed.attributes.getPermission().toString());
        
        parsed = parser.parseFTPEntry(parentPath, "+m825718503,,r,s280,\tdjb.html\r\n");
        assertEquals("X name", "djb.html", parsed.getName());
        assertFalse("X is dir", parsed.attributes.isDirectory());
        assertEquals("X size", 280, parsed.attributes.getSize(), 0);
        assertEquals("X timestamp", new Date(millis), parsed.attributes.getTimestamp());
        assertEquals("permissions", "--------- (000)", parsed.attributes.getPermission().toString());
        
        parsed = parser.parseFTPEntry(parentPath, "+m825718503,r,s280,,\tdjb.html\r\n");
        assertEquals("XX name", "djb.html", parsed.getName());
        assertFalse("XX is dir", parsed.attributes.isDirectory());
        assertEquals("XX size", 280, parsed.attributes.getSize(), 0);
        assertEquals("XX timestamp", new Date(millis), parsed.attributes.getTimestamp());
        assertEquals("permissions", "--------- (000)", parsed.attributes.getPermission().toString());
    }

    public void testNoFacts() {
        // We know nothing but the name of the mystery file, which essentially means a RETR or CWD is futile.
        assertNull(parser.parseFTPEntry(parentPath, "+\tMysteryFile"));
    }

    public void testAFewNotVeryInterestingFiles() {
        assertNull("dot", parser.parseFTPEntry(parentPath, "+/,\t."));
        assertNull("dot dot", parser.parseFTPEntry(parentPath, "+/,\t.."));
        assertNull("empty", parser.parseFTPEntry(parentPath, "+r,\t\r\n"));
        assertNull("really empty", parser.parseFTPEntry(parentPath, "+\t\r\n"));
    }

    public void testMissingNameSeparator() {
        assertNull(parser.parseFTPEntry(parentPath, "+r,s1234,  notabinfront\r\n"));
    }
}
