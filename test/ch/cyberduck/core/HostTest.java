package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * @version $Id$
 */
public class HostTest extends AbstractTestCase {

    public HostTest(String name) {
        super(name);
    }

    public void testParseURLEmpty() {
        Host h = Host.parse("");
        assertTrue(h.getHostname().equals(Preferences.instance().getProperty("connection.hostname.default")));
    }

    public void testParseURLFull() {
        {
            String url = "sftp://user:pass@hostname/path/to/file";
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("www.testrumpus.com"));
            assertTrue(h.getProtocol().equals(Protocol.WEBDAV));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getDefaultPath().equals("/"));
        }
    }

    public void testParseURLWithPortNumber() {
        {
            String url = "sftp://user:pass@hostname:999/path/to/file";
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
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

    public void testParseURLWithUsername() {
        {
            String url = "sftp://user@hostname/path/to/file";
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.SFTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftp://user@hostname/path/to/file";
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "ftps://user@hostname/path/to/file";
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(Protocol.FTP_TLS));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
    }

    public void testParseURLWithoutProtocol() {
        {
            String url = "user@hostname/path/to/file";
            Host h = Host.parse(url);
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
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user"));
            assertNull(h.getCredentials().getPassword());
        }
    }

    public void testParseWithTwoKlammeraffen() {
        {
            String url = "user@name@hostname";
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user@name"));
            assertNull(h.getCredentials().getPassword());
        }
        {
            String url = "user@name:password@hostname";
            Host h = Host.parse(url);
            assertTrue(h.getHostname().equals("hostname"));
            assertTrue(h.getProtocol().equals(
                    ProtocolFactory.forName(Preferences.instance().getProperty("connection.protocol.default"))));
            assertNotNull(h.getCredentials().getUsername());
            assertTrue(h.getCredentials().getUsername().equals("user@name"));
            assertTrue(h.getCredentials().getPassword().equals("password"));
        }
    }

    public void testParseURLWithDefaultPath() {
        {
            String url = "user@hostname/path/to/file";
            Host h = Host.parse(url);
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
        {
            String url = "user@hostname:999/path/to/file";
            Host h = Host.parse(url);
            assertTrue(h.getDefaultPath().equals("/path/to/file"));
        }
    }

    public void testWebURL() {
        {
            Host host = new Host("test");
            host.setWebURL("http://localhost/~dkocher");
            assertEquals("http://localhost/~dkocher", host.getWebURL());
        }
    }

    public void testAbsoluteDocumentRoot() {
        {
            Host host = new Host("localhost");
            host.setDefaultPath("/usr/home/dkocher/public_html");
            Path path = PathFactory.createPath(SessionFactory.createSession(host),
                    "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
            assertEquals("http://localhost/file", path.toHttpURL());
            host.setWebURL("http://127.0.0.1/~dkocher");
            assertEquals("http://127.0.0.1/~dkocher/file", path.toHttpURL());
        }
    }

    public void testRelativeDocumentRoot() {
        {
            Host host = new Host("localhost");
            host.setDefaultPath("public_html");
            Path path = PathFactory.createPath(SessionFactory.createSession(host),
                    "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
            assertEquals("http://localhost/file", path.toHttpURL());
            host.setWebURL("http://127.0.0.1/~dkocher");
            assertEquals("http://127.0.0.1/~dkocher/file", path.toHttpURL());
        }
    }

    public void testDefaultPathRoot() {
        {
            Host host = new Host("localhost");
            host.setDefaultPath("/");
            Path path = PathFactory.createPath(SessionFactory.createSession(host),
                    "/file", Path.DIRECTORY_TYPE);
            assertEquals("http://localhost/file", path.toHttpURL());
            host.setWebURL("http://127.0.0.1/~dkocher");
            assertEquals("http://127.0.0.1/~dkocher/file", path.toHttpURL());
        }
    }
}
