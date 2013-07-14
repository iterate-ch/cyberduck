package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class HostParserTest extends AbstractTestCase {

    @Test
    public void testParseURLEmpty() {
        Host h = HostParser.parse("");
        assertTrue(h.getHostname().equals(Preferences.instance().getProperty("connection.hostname.default")));
    }

    @Test
    public void testParseURLFull() {
        {
            String url = "sftp://user:pass@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.SFTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftp://user:pass@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftps://user:pass@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP_TLS));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "http://www.testrumpus.com/";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("www.testrumpus.com"));
            assertTrue(h.getProtocol().equals(Protocol.WEBDAV));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getDefaultPath().equals("/"));
        }
    }


    @Test
    public void testParseURLWithPortNumber() {
        {
            String url = "sftp://user:pass@hostname:999/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.SFTP));
            assertTrue(h.getPort() == 999);
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftp://user:pass@hostname:999/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP));
            assertTrue(h.getPort() == 999);
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftps://user:pass@hostname:999/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP_TLS));
            assertTrue(h.getPort() == 999);
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNotNull(h.getCredentials().getPassword());
            assertTrue(h.getCredentials().getPassword().equals("pass"));
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
    }

    @Test
    public void testParseURLWithUsername() {
        {
            String url = "sftp://user@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.SFTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftp://user@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftps://user@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP_TLS));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
    }

    @Test
    public void testParseURLWithoutProtocol() {
        {
            String url = "user@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "user@hostname";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
        }
    }

    @Test
    public void testParseWithTwoKlammeraffen() {
        {
            String url = "user@name@hostname";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user@name"));
            assertNull(h.getCredentials().getPassword());
        }
        {
            String url = "user@name:password@hostname";
            Host h = HostParser.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user@name"));
            assertTrue(h.getCredentials().getPassword().equals("password"));
        }
    }

    @Test
    public void testParseURLWithDefaultPath() {
        {
            String url = "user@hostname/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "user@hostname:999/path/to/file";
            Host h = HostParser.parse(url);
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
    }
}
