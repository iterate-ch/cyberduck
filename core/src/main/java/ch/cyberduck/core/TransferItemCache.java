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

public class TransferItemCache extends AbstractCache<TransferItem> {

    public static TransferItemCache empty() {
        return new TransferItemCache(0) {
            @Override
            public AttributedList<TransferItem> put(final TransferItem item, final AttributedList<TransferItem> children) {
                return AttributedList.emptyList();
            }
        };
    }

    public TransferItemCache(final int size) {
        super(size);
    }

    @Override
    protected CacheReference key(final TransferItem object) {
        return new DefaultPathPredicate(object.remote);
    }

    @Override
    public boolean isHidden(final TransferItem item) {
        return false;
    }
}
