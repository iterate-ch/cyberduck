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

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class LRUCache<Key, Value> {
    private static final Logger log = Logger.getLogger(LRUCache.class);

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader) {
        return usingLoader(loader, new NullListener<Key, Value>(), -1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final long maximumSize) {
        return usingLoader(loader, new NullListener<Key, Value>(), maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener,
                                                                final long maximumSize, final long expireDuration) {
        return new LRUCache<>(loader, listener, maximumSize, expireDuration);
    }

    public static <Key, Value> LRUCache<Key, Value> build() {
        return build(-1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final long maximumSize) {
        return build(maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final long maximumSize, final long expireDuration) {
        return new LRUCache<>(null, new NullListener<>(), maximumSize, expireDuration);
    }

    private final Cache<Key, Value> delegate;

    private LRUCache(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration) {
        final CacheBuilder<Key, Value> builder = CacheBuilder.newBuilder()
            .removalListener(new RemovalListener<Key, Value>() {
                @Override
                public void onRemoval(final RemovalNotification<Key, Value> notification) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Removed %s from cache with cause %s", notification.getKey(), notification.getCause()));
                    }
                    listener.onRemoval(notification);
                }
            });
        if(maximumSize > 0) {
            builder.maximumSize(maximumSize);
        }
        if(expireDuration > 0) {
            builder.expireAfterAccess(expireDuration, TimeUnit.MILLISECONDS);
        }

        if(loader != null) {
            delegate = builder.build(new CacheLoader<Key, Value>() {
                @Override
                public Value load(Key key) {
                    return loader.apply(key);
                }
            });
        }
        else {
            delegate = builder.build();
        }
    }

    public Value get(final Key key) throws UncheckedExecutionException {
        if(delegate instanceof LoadingCache) {
            return ((LoadingCache<Key, Value>) delegate).getUnchecked(key);
        }
        return delegate.getIfPresent(key);
    }

    public Map<Key, Value> asMap() {
        return delegate.asMap();
    }

    public void put(final Key key, Value value) {
        delegate.put(key, value);
    }

    public void remove(final Key key) {
        delegate.invalidate(key);
    }

    public long size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.size() == 0;
    }

    public boolean contains(final Key key) {
        return null != delegate.getIfPresent(key);
    }

    public void clear() {
        delegate.invalidateAll();
    }

    private static class NullListener<Key, Value> implements RemovalListener<Key, Value> {

        @Override
        public void onRemoval(final RemovalNotification<Key, Value> notification) {
            //
        }
    }
}
