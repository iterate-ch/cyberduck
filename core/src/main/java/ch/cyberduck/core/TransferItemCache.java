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

import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.transfer.TransferItem;

public class TransferItemCache extends AbstractCache<TransferItem> {

    private static final CacheReference<TransferItem> NULL_KEY = new TransferItemCacheReference(new TransferItem(Home.ROOT));

    public TransferItemCache(final int size) {
        super(size);
    }

    @Override
    public CacheReference<TransferItem> reference(final TransferItem object) {
        if(null == object) {
            return NULL_KEY;
        }
        return new TransferItemCacheReference(object);
    }

    public static final class TransferItemCacheReference implements CacheReference<TransferItem> {
        private final CacheReference<Path> proxy;

        public TransferItemCacheReference(final TransferItem object) {
            this.proxy = new DefaultPathPredicate(object.remote);
        }

        @Override
        public boolean test(final TransferItem other) {
            return proxy.test(other.remote);
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o instanceof CacheReference) {
                return proxy.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return proxy.hashCode();
        }

        @Override
        public String toString() {
            return proxy.toString();
        }
    }
}
