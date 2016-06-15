package ch.cyberduck.core;

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

import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TransferItemCacheTest {

    @Test
    public void testRemove() throws Exception {
        final AttributedList<TransferItem> remove = new TransferItemCache(1).remove(new TransferItem(new Path("/t", EnumSet.of(Path.Type.directory))));
        assertNotNull(remove);
        assertTrue(remove.isEmpty());
    }

    @Test
    public void testLookup() throws Exception {
        final TransferItemCache c = new TransferItemCache(1);
        final AttributedList<TransferItem> list = new AttributedList<>();
        list.add(new TransferItem(new Path("/r2", EnumSet.of(Path.Type.file)), new Local("/l2")));
        c.put(new TransferItem(new Path("/r", EnumSet.of(Path.Type.directory)), new Local("/l")), list);
        assertNotNull(c.lookup(new DefaultPathReference(new Path("/r2", EnumSet.of(Path.Type.file)))));
    }

    @Test
    public void testLookupFromRootDirectory() throws Exception {
        final TransferItemCache c = new TransferItemCache(1);
        final AttributedList<TransferItem> list = new AttributedList<>();
        list.add(new TransferItem(new Path("/r2", EnumSet.of(Path.Type.file)), new Local("/l2")));
        c.put(null, list);
        assertNotNull(c.lookup(new DefaultPathReference(new Path("/r2", EnumSet.of(Path.Type.file)))));
    }
}