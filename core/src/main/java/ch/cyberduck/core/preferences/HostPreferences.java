package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.cache.LRUCache;

import org.apache.commons.lang3.StringUtils;

public class HostPreferences implements PreferencesReader {

    private final LRUCache<String, String> cache = LRUCache.usingLoader(this::loadProperty, 1000);

    private static final String MISSING_PROPERTY = String.valueOf(StringUtils.INDEX_NOT_FOUND);

    private final PreferencesReader proxy;
    private final Host bookmark;

    public HostPreferences(final Host bookmark) {
        this(bookmark, PreferencesFactory.get());
    }

    public HostPreferences(final Host bookmark, final PreferencesReader proxy) {
        this.bookmark = bookmark;
        this.proxy = proxy;
    }

    /**
     * Delete cached properties.
     */
    public void clear() {
        cache.clear();
    }

    @Override
    public String getProperty(final String key) {
        final String value = cache.get(key);
        if(StringUtils.equals(MISSING_PROPERTY, value)) {
            return proxy.getProperty(key);
        }
        return value;
    }

    public void setProperty(final String key, final String value) {
        cache.put(key, value);
        bookmark.setProperty(key, value);
    }

    private String loadProperty(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return MISSING_PROPERTY;
        }
        return value;
    }
}
