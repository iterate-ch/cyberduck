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

import ch.cyberduck.core.cache.LRUCache;

import org.apache.log4j.Logger;

import java.util.Map;

public class ReverseLookupCache<T extends Referenceable> implements Cache<T> {
    private static final Logger log = Logger.getLogger(ReverseLookupCache.class);

    private final Cache<T> proxy;
    private final LRUCache<CacheReference, T> reverse;

    public ReverseLookupCache(final Cache<T> proxy, final int size) {
        this.proxy = proxy;
        if(size == Integer.MAX_VALUE) {
            // Unlimited
            reverse = LRUCache.build();
        }
        else {
            reverse = LRUCache.build(size);
        }
    }

    @Override
    public CacheReference key(final T object) {
        return proxy.key(object);
    }

    @Override
    public boolean isCached(final T parent) {
        return proxy.isCached(parent);
    }

    @Override
    public boolean isEmpty() {
        return proxy.isEmpty();
    }

    @Override
    public boolean isValid(final T item) {
        return proxy.isValid(item);
    }

    @Override
    public AttributedList<T> put(final T reference, final AttributedList<T> children) {
        for(T f : children) {
            final CacheReference key = proxy.key(f);
            reverse.remove(key);
            reverse.put(key, f);
        }
        return proxy.put(reference, children);
    }

    @Override
    public AttributedList<T> get(final T parent) {
        return proxy.get(parent);
    }

    /**
     * Lookup a path by reference in the cache.
     *
     * @param reference A child object of a cached directory listing in the cache
     * @return Null if the path is not in the cache
     * @see ch.cyberduck.core.AttributedList#get(Referenceable)
     */
    public T lookup(final CacheReference reference) {
        final T value = reverse.get(reference);
        if(null == value) {
            log.warn(String.format("Lookup failed for %s in reverse cache", reference));
        }
        return value;
    }

    public AttributedList<T> remove(final T reference) {
        final AttributedList<T> removed = proxy.remove(reference);
        for(T r : removed) {
            reverse.remove(proxy.key(r));
        }
        return removed;
    }

    @Override
    public Map<T, AttributedList<T>> asMap() {
        return proxy.asMap();
    }

    @Override
    public long size() {
        return proxy.size();
    }

    @Override
    public void invalidate(final T parent) {
        proxy.invalidate(parent);
    }

    @Override
    public void clear() {
        proxy.clear();
        reverse.clear();
    }
}
