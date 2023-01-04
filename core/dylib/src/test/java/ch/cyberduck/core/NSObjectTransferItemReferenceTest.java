package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import static org.junit.Assert.assertEquals;

public class NSObjectTransferItemReferenceTest {

    @Test
    public void testEquals() {
        final Path f = new Path("/f", EnumSet.of(Path.Type.file));
        assertEquals(new TransferItemCache.TransferItemCacheReference(new TransferItem(f)), new TransferItemCache.TransferItemCacheReference(new TransferItem(f)));
        assertEquals(new TransferItemCache.TransferItemCacheReference(new TransferItem(f)).hashCode(), new NSObjectPathReference(NSObjectTransferItemReference.get(f)).hashCode());
        assertEquals(new TransferItemCache.TransferItemCacheReference(new TransferItem(f)).toString(), new NSObjectPathReference(NSObjectTransferItemReference.get(f)).toString());
        assertEquals(new TransferItemCache.TransferItemCacheReference(new TransferItem(f)), new NSObjectPathReference(NSObjectTransferItemReference.get(f)));
    }
}