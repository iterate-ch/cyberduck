package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import static org.junit.Assert.*;

public class AbstractHostCollectionTest {

    @Test
    public void testLookup() {
        final AbstractHostCollection c = new AbstractHostCollection() {
        };
        final Host bookmark = new Host(new TestProtocol());
        assertFalse(c.find(new AbstractHostCollection.HostComparePredicate(bookmark)).isPresent());
        assertNull(c.lookup(bookmark.getUuid()));
        c.add(bookmark);
        assertTrue(c.find(new AbstractHostCollection.HostComparePredicate(bookmark)).isPresent());
        assertNotNull(c.lookup(bookmark.getUuid()));
    }

    @Test
    public void testRevealBookmark() {
        final AbstractHostCollection c = new AbstractHostCollection() {
        };
        final Host bookmark = new Host(new TestProtocol(), "h", new Credentials("u"));
        assertFalse(c.find(new AbstractHostCollection.HostComparePredicate(bookmark)).isPresent());
        assertNull(c.lookup(bookmark.getUuid()));
        c.add(bookmark);
        assertTrue(c.find(bookmark).isPresent());
        assertTrue(c.find(new Host(new TestProtocol(), "h", new Credentials("u"))).isPresent());
        assertTrue(c.find(new Host(new TestProtocol(), "h")).isPresent());
        assertFalse(c.find(new Host(new TestProtocol())).isPresent());
        assertFalse(c.find(new Host(new TestProtocol(), "h", new Credentials("u2"))).isPresent());
    }
}