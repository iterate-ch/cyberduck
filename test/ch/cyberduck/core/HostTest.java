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

    public void testParseURLFull() {
        try {
            {
                String url = "sftp://user:pass@hostname/path/to/file";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(Session.SFTP));
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
                assertTrue(h.getProtocol().equals(Session.FTP));
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
                assertTrue(h.getProtocol().equals(Session.FTP_TLS));
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
                assertTrue(h.getProtocol().equals(Session.SFTP));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user"));
                assertNull(h.getCredentials().getPassword());
                assertTrue(h.getDefaultPath().equals("/path/to/file"));
            }
            {
                String url = "ftp://user@hostname/path/to/file";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(Session.FTP));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user"));
                assertNull(h.getCredentials().getPassword());
                assertTrue(h.getDefaultPath().equals("/path/to/file"));
            }
            {
                String url = "ftps://user@hostname/path/to/file";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(Session.FTP_TLS));
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
                assertTrue(h.getProtocol().equals(Preferences.instance().getProperty("connection.protocol.default")));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user"));
                assertNull(h.getCredentials().getPassword());
                assertTrue(h.getDefaultPath().equals("/path/to/file"));
            }
            {
                String url = "user@hostname";
                Host h = Host.parse(url);
                assertTrue(h.getHostname().equals("hostname"));
                assertTrue(h.getProtocol().equals(Preferences.instance().getProperty("connection.protocol.default")));
                assertNotNull(h.getCredentials().getUsername());
                assertTrue(h.getCredentials().getUsername().equals("user"));
                assertNull(h.getCredentials().getPassword());
            }
        }
        catch(MalformedURLException e) {
            fail(e.getMessage());
        }
    }
}
