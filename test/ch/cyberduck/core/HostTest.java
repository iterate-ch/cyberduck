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

import junit.framework.TestCase;

import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public class HostTest extends TestCase {

    public HostTest(String name) {
        super(name);
    }

    public void testClone() throws Exception {
        Host host = new Host("ftp.cyberduck.ch");
        Host clone = (Host)host.clone();
        assertNotSame(clone, host);
        assertNotSame(clone.getCredentials(), host.getCredentials());        
    }

    public void testParseURLFull() {
        try {
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
        }
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    public void testParseURLWithPortNumber() {
        try {
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
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    public void testParseURLWithUsername() {
        try {
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
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    public void testParseURLWithoutProtocol() {
        try {
            {
                String url = "user@hostname/path/to/file";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(
                        Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"))));
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
                        Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"))));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user"));
                assertNull(h.getCredentials().getPassword());
            }
        }
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    public void testParseWithTwoKlammeraffen() {
        try {
            {
                String url = "user@name@hostname";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(
                        Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"))));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user@name"));
                assertNull(h.getCredentials().getPassword());
            }
            {
                String url = "user@name:password@hostname";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(
                        Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"))));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user@name"));
                assertTrue(h.getCredentials().getPassword().equals("password"));
            }
        }
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    public void testParseURLWithDefaultPath() {
        try {
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
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }
}
