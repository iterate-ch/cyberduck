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

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testGroups() {
        final AbstractHostCollection c = new AbstractHostCollection() {
        };
        final Host bookmarkGroupa1 = new Host(new TestProtocol(), "h", new Credentials("u"));
        bookmarkGroupa1.setLabels(Collections.singleton("a"));
        final Host bookmarkGroupA1 = new Host(new TestProtocol(), "h", new Credentials("u"));
        bookmarkGroupA1.setLabels(Collections.singleton("A"));
        bookmarkGroupA1.setNickname("a");
        final Host bookmarkGroupA2 = new Host(new TestProtocol(), "h", new Credentials("u"));
        bookmarkGroupA2.setLabels(Collections.singleton("A"));
        bookmarkGroupA2.setNickname("b");
        final Host bookmarkGroupB = new Host(new TestProtocol(), "h", new Credentials("u"));
        bookmarkGroupB.setLabels(Collections.singleton("B"));
        c.add(bookmarkGroupa1);
        c.add(bookmarkGroupB);
        c.add(bookmarkGroupA2);
        c.add(bookmarkGroupA1);
        final Map<String, List<Host>> groups = c.groups();
        assertEquals("a", groups.keySet().toArray()[0]);
        assertEquals("A", groups.keySet().toArray()[1]);
        assertEquals("a", groups.get("A").toArray(new Host[0])[0].getNickname());
        assertEquals("b", groups.get("A").toArray(new Host[0])[1].getNickname());
        assertEquals("B", groups.keySet().toArray()[2]);
    }
}