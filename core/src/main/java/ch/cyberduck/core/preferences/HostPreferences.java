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

import java.util.List;

public class HostPreferences implements PreferencesReader {

    private final PreferencesReader proxy;
    private final Host bookmark;

    public HostPreferences(final Host bookmark) {
        this(bookmark, PreferencesFactory.get());
    }

    public HostPreferences(final Host bookmark, final PreferencesReader proxy) {
        this.bookmark = bookmark;
        this.proxy = proxy;
    }

    @Override
    public String getProperty(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getProperty(key);
        }
        return value;
    }

    @Override
    public List<String> getList(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getList(key);
        }
        return PreferencesReader.toList(value);
    }

    @Override
    public int getInteger(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getInteger(key);
        }
        return PreferencesReader.toInteger(value);
    }

    @Override
    public float getFloat(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getFloat(key);
        }
        return PreferencesReader.toFloat(value);
    }

    @Override
    public long getLong(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getLong(key);
        }
        return PreferencesReader.toLong(value);
    }

    @Override
    public double getDouble(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getDouble(key);
        }
        return PreferencesReader.toDouble(value);
    }

    @Override
    public boolean getBoolean(final String key) {
        final String value = bookmark.getProperty(key);
        if(null == value) {
            return proxy.getBoolean(key);
        }
        return PreferencesReader.toBoolean(value);
    }
}
