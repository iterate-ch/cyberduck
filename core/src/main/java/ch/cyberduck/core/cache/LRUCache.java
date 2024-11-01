package ch.cyberduck.core.cache;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

public class LRUCache<Key, Value> {
    private static final Logger log = LogManager.getLogger(LRUCache.class);

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader) {
        return usingLoader(loader, null, -1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener) {
        return usingLoader(loader, listener, -1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final long maximumSize) {
        return usingLoader(loader, null, maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener, final long maximumSize) {
        return usingLoader(loader, listener, maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration) {
        return usingLoader(loader, listener, maximumSize, expireDuration, true);
    }

    public static <Key, Value> LRUCache<Key, Value> usingLoader(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration, boolean expireAfterAccess) {
        return new LRUCache<>(loader, listener, maximumSize, expireDuration, expireAfterAccess, null);
    }

    public static <Key, Value> LRUCache<Key, Value> build() {
        return build(null, -1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final RemovalListener<Key, Value> listener) {
        return build(listener, -1L, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final long maximumSize) {
        return build(null, maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final RemovalListener<Key, Value> listener, final long maximumSize) {
        return build(listener, maximumSize, -1L);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration) {
        return build(listener, maximumSize, expireDuration, true);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration, boolean expireAfterAccess) {
        return new LRUCache<>(null, listener, maximumSize, expireDuration, expireAfterAccess, null);
    }

    public static <Key, Value> LRUCache<Key, Value> build(final RemovalListener<Key, Value> listener, final long maximumSize, final Expiry<Key, Value> expiry) {
        return new LRUCache<>(null, listener, maximumSize, -1L, true, expiry);
    }

    private final Cache<Key, Value> delegate;

    private LRUCache(final Function<Key, Value> loader, final RemovalListener<Key, Value> listener, final long maximumSize, final long expireDuration, boolean expireAfterAccess, final Expiry<Key, Value> expiry) {
        final Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if(listener != null) {
            builder.removalListener(new RemovalListener<Key, Value>() {
                @Override
                public void onRemoval(final Key key, final Value value, final RemovalCause cause) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Removed %s from cache with cause %s", key, cause));
                    }
                    listener.onRemoval(key, value, cause);
                }
            });
        }
        if(maximumSize > 0) {
            builder.maximumSize(maximumSize);
        }
        if(expiry != null) {
            builder.expireAfter(expiry);
        }
        if(expireDuration > 0) {
            if(expireAfterAccess) {
                builder.expireAfterAccess(expireDuration, TimeUnit.MILLISECONDS);
            }
            else {
                builder.expireAfterWrite(expireDuration, TimeUnit.MILLISECONDS);
            }
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

    public Value get(final Key key) {
        if(delegate instanceof LoadingCache) {
            return ((LoadingCache<Key, Value>) delegate).get(key);
        }
        return delegate.getIfPresent(key);
    }

    public Map<Key, Value> asMap() {
        return Collections.unmodifiableMap(delegate.asMap());
    }

    public void put(final Key key, Value value) {
        if(null == key) {
            log.warn(String.format("Discard caching %s=%s", key, value));
            return;
        }
        if(null == value) {
            this.remove(key);
        }
        else {
            delegate.put(key, value);
        }
    }

    public void remove(final Key key) {
        delegate.invalidate(key);
    }

    public long size() {
        return delegate.estimatedSize();
    }

    public boolean isEmpty() {
        return delegate.estimatedSize() == 0;
    }

    public boolean contains(final Key key) {
        return null != delegate.getIfPresent(key);
    }

    public void clear() {
        delegate.invalidateAll();
    }

    /**
     * Performs any pending maintenance operations needed by the cache
     */
    public void evict() {
        delegate.cleanUp();
    }
}
