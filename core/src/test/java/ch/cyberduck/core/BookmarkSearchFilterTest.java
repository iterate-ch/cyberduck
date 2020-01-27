package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BookmarkSearchFilterTest {

    @Test
    public void accept() {
        final Host bookmark = new Host(new TestProtocol(Scheme.http), "a");
        assertTrue(new BookmarkSearchFilter("b a").accept(bookmark));
        assertFalse(new BookmarkSearchFilter("b testa").accept(bookmark));
        assertFalse(new BookmarkSearchFilter("b b").accept(bookmark));
        assertTrue(new BookmarkSearchFilter("HTTP").accept(bookmark));
        bookmark.setNickname("t");
        assertTrue(new BookmarkSearchFilter("t").accept(bookmark));
        assertFalse(new BookmarkSearchFilter("t2").accept(bookmark));
        bookmark.setLabels(Collections.singleton("l"));
        assertTrue(new BookmarkSearchFilter("l").accept(bookmark));
        assertFalse(new BookmarkSearchFilter("l2").accept(bookmark));
    }
}
