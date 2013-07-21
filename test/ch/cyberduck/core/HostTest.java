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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class HostTest extends AbstractTestCase {

    @Test
    public void testDictionary() {
        final Host h = new Host(Protocol.WEBDAV, "h", 66);
        assertEquals(h, new Host(h.getAsDictionary()));
    }

    @Test
    public void testWebURL() {

        Host host = new Host("test");
        host.setWebURL("http://localhost/~dkocher");
        assertEquals("http://localhost/~dkocher", host.getWebURL());

    }

    @Test
    public void testAbsoluteDocumentRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("/usr/home/dkocher/public_html");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", session.toHttpURL(path));
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", session.toHttpURL(path));
    }

    @Test
    public void testRelativeDocumentRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("public_html");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", session.toHttpURL(path));
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", session.toHttpURL(path));
    }

    @Test
    public void testDefaultPathRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("/");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", session.toHttpURL(path));
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", session.toHttpURL(path));
    }

    @Test
    public void testDownloadFolder() {
        Host host = new Host("localhost");
        assertTrue("~/Desktop".equals(host.getDownloadFolder().getAbbreviatedPath()) || "~/Downloads".equals(host.getDownloadFolder().getAbbreviatedPath()));
        host.setDownloadFolder(LocalFactory.createLocal("/t"));
        assertEquals("/t", host.getDownloadFolder().getAbbreviatedPath());
    }

    @Test
    public void testToUrl() {
        assertEquals("sftp://user@localhost", new Host(Protocol.SFTP, "localhost", new Credentials("user", "p") {
            @Override
            public String getUsernamePlaceholder() {
                return null;
            }

            @Override
            public String getPasswordPlaceholder() {
                return null;
            }
        }).toURL(true));
        assertEquals("sftp://localhost", new Host(Protocol.SFTP, "localhost", new Credentials("user", "p") {
            @Override
            public String getUsernamePlaceholder() {
                return null;
            }

            @Override
            public String getPasswordPlaceholder() {
                return null;
            }
        }).toURL(false));
        assertEquals("sftp://localhost:222", new Host(Protocol.SFTP, "localhost", 222).toURL(false));
    }
}
