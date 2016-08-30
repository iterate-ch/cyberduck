package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Expandrive5BookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new Expandrive5BookmarkCollection().parse(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        Expandrive5BookmarkCollection c = new Expandrive5BookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("src/test/resources/expandrive5.favorites.js"));
        assertEquals(2, c.size());
        assertEquals("Imported from ExpanDrive 5", c.get(0).getComment());
        assertEquals("c", c.get(0).getCredentials().getUsername());
        assertEquals("IAD", c.get(1).getRegion());
        assertEquals("Cloud Files", c.get(1).getNickname());
    }
}