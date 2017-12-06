package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
        final Cache<TransferItem> c = new ReverseLookupCache<TransferItem>(new TransferItemCache(1), 1);
        final AttributedList<TransferItem> list = new AttributedList<>();
        list.add(new TransferItem(new Path("/r2", EnumSet.of(Path.Type.file)), new Local("/l2")));
        c.put(new TransferItem(new Path("/r", EnumSet.of(Path.Type.directory)), new Local("/l")), list);
        final NSObjectTransferItemReference reference = new NSObjectTransferItemReference(NSObjectTransferItemReference.get(new Path("/r2", EnumSet.of(Path.Type.file))));
        assertNotNull(c.lookup(reference));
    }

    @Test
    public void testLookupFromRootDirectory() throws Exception {
        final Cache<TransferItem> c = new ReverseLookupCache<TransferItem>(new TransferItemCache(1), 1);
        final AttributedList<TransferItem> list = new AttributedList<>();
        list.add(new TransferItem(new Path("/r2", EnumSet.of(Path.Type.file)), new Local("/l2")));
        c.put(null, list);
        final NSObjectTransferItemReference reference = new NSObjectTransferItemReference(NSObjectTransferItemReference.get(new Path("/r2", EnumSet.of(Path.Type.file))));
        assertNotNull(c.lookup(reference));
    }
}
