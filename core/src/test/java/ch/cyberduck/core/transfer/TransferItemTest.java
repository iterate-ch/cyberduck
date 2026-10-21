package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TransferItemTest {

    @Test
    public void testEqualsSubclass() {
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        final TransferItem item = new TransferItem(file);
        final TransferItem subclass = new TransferItem(file) {
        };
        assertEquals(item, subclass);
        assertEquals(subclass, item);
        assertEquals(item.hashCode(), subclass.hashCode());
        final Map<TransferItem, Boolean> map = new HashMap<>();
        map.put(item, true);
        assertTrue(map.containsKey(subclass));
    }

    @Test
    public void testNotEqualsDifferentPath() {
        assertNotEquals(new TransferItem(new Path("/a", EnumSet.of(Path.Type.file))),
                new TransferItem(new Path("/b", EnumSet.of(Path.Type.file))));
    }
}
