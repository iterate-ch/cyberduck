package ch.cyberduck.core.cache;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class LRUCache<Key, Value> {

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final long maximumSize) {
        return new LRUCache<>(loader, maximumSize);
    }

    private final LoadingCache<Key, Value> delegate;

    private LRUCache(final Function<Key, Value> loader, final long maximumSize) {
        delegate = CacheBuilder.newBuilder()
            .maximumSize(maximumSize)
            .build(new CacheLoader<Key, Value>() {
                @Override
                public Value load(Key key) {
                    return loader.apply(key);
                }
            });
    }

    public Value get(final Key key) throws UncheckedExecutionException {
        return delegate.getUnchecked(key);
    }

    public void forEach(final BiConsumer<Key, Value> function) {
        delegate.asMap().forEach(function);
    }

    public void put(final Key key, Value value) {
        delegate.put(key, value);
    }

    public void remove(final Key key) {
        delegate.invalidate(key);
    }
}
