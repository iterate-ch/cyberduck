// An extension to the UnixFTPEntryParser, making it understand EPLF,
// as described in http://cr.yp.to/ftp/list/eplf.html .

package org.apache.commons.net.ftp.parser;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Login;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.FTPSession;

import java.util.Date;

public class EPLFEntryParserTest extends junit.framework.TestCase {

    static {
		org.apache.log4j.BasicConfigurator.configure();
	}
    
    public EPLFEntryParserTest(String name) {
        super(name);
    }
    
    private UnixFTPEntryParser parser;
    private Login login;
    private Host host;
    private FTPSession session;
    private Path parentPath;

    public void setUp() {
        this.parser = new UnixFTPEntryParser();
        this.login = new Login("localhost", "anonymous", "anonymous@example.net", false);
        this.host = new Host("localhost", login);
        this.session = new FTPSession(host);
        this.parentPath = PathFactory.createPath(session, "/");
    }

    public void testStandardBinls() {
        // Legacy test.
        // Just to make sure we don't break the standard parser.
        Path parsed = parser.parseFTPEntry(parentPath, "drwxrwxr-x   7 root     ftpadmin     1024 Apr 20 16:17 pub");

        this.assertTrue("is dir", parsed.attributes.isDirectory());
        this.assertFalse("is root", parsed.isRoot());
        this.assertEquals("pub", parsed.getName());
        this.assertEquals("/pub", parsed.getAbsolute());

        this.assertEquals(1024, parsed.status.getSize());

        this.assertNotNull("timestamp", parsed.attributes.getTimestamp());
        this.assertEquals(Path.DIRECTORY_TYPE, parsed.attributes.getType());
        this.assertTrue("attr is dir", parsed.attributes.isDirectory());
        this.assertFalse("attr is file", parsed.attributes.isFile());
        this.assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        this.assertEquals("root", parsed.attributes.getOwner());
        this.assertEquals("ftpadmin", parsed.attributes.getGroup());

        this.assertEquals("drwxrwxr-x (775)", parsed.attributes.getPermission().toString());
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

        this.assertEquals("name", "djb.html", parsed.getName());
        this.assertEquals("absolute", "/djb.html", parsed.getAbsolute());
        this.assertFalse("is dir", parsed.attributes.isDirectory());
        this.assertFalse("is root", parsed.isRoot());
        
        this.assertEquals("size", 280, parsed.status.getSize());
        
        long millis = 825718503;
        millis = millis * 1000;
        this.assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());
        this.assertEquals("type", Path.FILE_TYPE, parsed.attributes.getType());
        this.assertFalse("attr is dir", parsed.attributes.isDirectory());
        this.assertTrue("attr is file", parsed.attributes.isFile());
        this.assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        this.assertEquals("owner", "unknown", parsed.attributes.getOwner());
        this.assertEquals("group", "unknown", parsed.attributes.getGroup());
        
        this.assertEquals("permissions", "-r--r--r-- (444)", parsed.attributes.getPermission().toString());
    }
    
    public void testReadonlyDirectory() {
        Path parsed = parser.parseFTPEntry(parentPath, "+m825718503,/,\t514");
        
        this.assertEquals("name", "514", parsed.getName());
        this.assertEquals("absolute", "/514", parsed.getAbsolute());
        this.assertTrue("is dir", parsed.attributes.isDirectory());
        this.assertFalse("is root", parsed.isRoot());
        
        this.assertEquals("size", 0, parsed.status.getSize());
        
        long millis = 825718503;
        millis = millis * 1000;
        this.assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());
        this.assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        this.assertTrue("attr is dir", parsed.attributes.isDirectory());
        this.assertFalse("attr is file", parsed.attributes.isFile());
        this.assertFalse("attr is link", parsed.attributes.isSymbolicLink());
        this.assertEquals("owner", "unknown", parsed.attributes.getOwner());
        this.assertEquals("group", "unknown", parsed.attributes.getGroup());
        
        this.assertEquals("permissions", "dr-xr-xr-x (555)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsOverrideStandardDirPermissions() {
        Path parsed = parser.parseFTPEntry(parentPath, "+up153,/,\t514");
        this.assertTrue("is dir", parsed.attributes.isDirectory());
        this.assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        this.assertTrue("attr is dir", parsed.attributes.isDirectory());
        this.assertEquals("d--xr-x-wx (153)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsDoesntRemoveDirTag() {
        Path parsed = parser.parseFTPEntry(parentPath, "+/,up153,\t514");
        this.assertTrue("is dir", parsed.attributes.isDirectory());
        this.assertEquals("type", Path.DIRECTORY_TYPE, parsed.attributes.getType());
        this.assertTrue("attr is dir", parsed.attributes.isDirectory());
        this.assertEquals("d--xr-x-wx (153)", parsed.attributes.getPermission().toString());
    }
    
    public void testSpecifiedPermissionsOverrideStandardFilePermissions() {
        Path parsed = parser.parseFTPEntry(parentPath, "+up153,r,\tmyfile");
        this.assertFalse("is dir", parsed.attributes.isDirectory());
        this.assertEquals("type", Path.FILE_TYPE, parsed.attributes.getType());
        this.assertTrue("attr is file", parsed.attributes.isFile());
        this.assertEquals("---xr-x-wx (153)", parsed.attributes.getPermission().toString());
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
        this.assertEquals("name", "djb.html", parsed.getName());
        this.assertFalse("is dir", parsed.attributes.isDirectory());
        this.assertEquals("size", 280, parsed.status.getSize());        
        this.assertEquals("timestamp", new Date(millis), parsed.attributes.getTimestamp());        
        this.assertEquals("permissions", "-r--r--r-- (444)", parsed.attributes.getPermission().toString());
        
        parsed = parser.parseFTPEntry(parentPath, "+m825718503,,r,s280,\tdjb.html\r\n");
        this.assertEquals("X name", "djb.html", parsed.getName());
        this.assertFalse("X is dir", parsed.attributes.isDirectory());
        this.assertEquals("X size", 280, parsed.status.getSize());        
        this.assertEquals("X timestamp", new Date(millis), parsed.attributes.getTimestamp());        
        this.assertEquals("X permissions", "-r--r--r-- (444)", parsed.attributes.getPermission().toString());
        
        parsed = parser.parseFTPEntry(parentPath, "+m825718503,r,s280,,\tdjb.html\r\n");
        this.assertEquals("XX name", "djb.html", parsed.getName());
        this.assertFalse("XX is dir", parsed.attributes.isDirectory());
        this.assertEquals("XX size", 280, parsed.status.getSize());        
        this.assertEquals("XX timestamp", new Date(millis), parsed.attributes.getTimestamp());        
        this.assertEquals("XX permissions", "-r--r--r-- (444)", parsed.attributes.getPermission().toString());
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
