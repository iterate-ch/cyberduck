package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DownloadDirectoryFinderTest {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new TestProtocol());
        assertNull(host.getDownloadFolder());
        final DownloadDirectoryFinder finder = new DownloadDirectoryFinder();
        assertEquals(System.getProperty("user.dir"), finder.find(host).getAbsolute());
        // Does not exist
        host.setDownloadFolder(new Local(String.format("/%s", UUID.randomUUID())));
        assertEquals(System.getProperty("user.dir"), finder.find(host).getAbsolute());
        final Local folder = new Local("~/Documents");
        folder.mkdir();
        host.setDownloadFolder(folder);
        assertEquals("~/Documents", finder.find(host).getAbbreviatedPath());
    }
}