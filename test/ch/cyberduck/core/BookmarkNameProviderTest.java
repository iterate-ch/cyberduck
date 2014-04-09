package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.dav.DAVProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class BookmarkNameProviderTest extends AbstractTestCase {

    @Test
    public void testToString() throws Exception {
        assertEquals("h – FTP", BookmarkNameProvider.toString(new Host("h")));
        assertEquals("h – WebDAV (HTTP)", BookmarkNameProvider.toString(new Host(new DAVProtocol(), "h")));
        assertEquals("h – WebDAV (HTTP)", BookmarkNameProvider.toString(new Host(new DAVProtocol(), "h", new Credentials("u", null))));
        assertEquals("u@h – WebDAV (HTTP)", BookmarkNameProvider.toString(new Host(new DAVProtocol(), "h", new Credentials("u", null)), true));
        assertEquals("h – WebDAV (HTTP)", BookmarkNameProvider.toString(new Host(new DAVProtocol(), "h", new Credentials(null, null)), true));
        assertEquals("h – WebDAV (HTTP)", BookmarkNameProvider.toString(new Host(new DAVProtocol(), "h", new Credentials("", null)), true));
    }
}
