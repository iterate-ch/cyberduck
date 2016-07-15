package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Transmit4BookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new Transmit4BookmarkCollection().parse(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testGetFile() throws Exception {
        Transmit4BookmarkCollection c = new Transmit4BookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("src/test/resources/Favorites.xml"));
        assertEquals(3, c.size());
    }
}