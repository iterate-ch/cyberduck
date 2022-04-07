package ch.cyberduck.ui.comparator;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class VersionsComparatorTest {

    @Test
    public void testCompareFirst() {
        final Path p1 = new Path("/a", EnumSet.of(Path.Type.file));
        final Path p2 = new Path("/b", EnumSet.of(Path.Type.file));
        assertEquals(0, new VersionsComparator(true).compareFirst(p1, p2));
        p1.attributes().setDuplicate(true);
        assertEquals(-1, new VersionsComparator(true).compareFirst(p1, p2));
        final long ts = System.currentTimeMillis();
        p1.attributes().setModificationDate(ts);
        p2.attributes().setModificationDate(ts);
        assertEquals(-1, new VersionsComparator(true).compareFirst(p1, p2));
        p2.attributes().setModificationDate(ts - 1000);
        assertEquals(-1, new VersionsComparator(true).compareFirst(p1, p2));
        p1.attributes().setDuplicate(false);
        assertEquals(1, new VersionsComparator(true).compareFirst(p1, p2));
    }
}