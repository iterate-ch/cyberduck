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

import ch.cyberduck.core.serializer.Serializer;

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
    public void testDeserialize() throws Exception {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey("swift", "Protocol");
        dict.setStringForKey("unknown provider", "Provider");
        dict.setStringForKey("h", "Hostname");
        final Host host = new Host(dict.getSerialized());
        assertEquals(Protocol.SWIFT, host.getProtocol());
    }

    @Test
    public void testWebURL() {
        Host host = new Host("test");
        host.setWebURL("http://localhost/~dkocher");
        assertEquals("http://localhost/~dkocher", host.getWebURL());

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
