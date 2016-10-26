package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.collections4.map.LRUMap;

import java.util.Map;

/**
 * Mapper between path references returned from the outline view model and its internal
 * string representation.
 */
public class NSObjectPathReference implements CacheReference<Path> {

    private static final Map<Path, NSString> cache = new LRUMap<Path, NSString>(
            PreferencesFactory.get().getInteger("browser.model.cache.size")
    );

    public static NSObject get(final Path file) {
        if(!cache.containsKey(file)) {
            cache.put(file, NSString.stringWithString(new DefaultPathReference(file).toString()));
        }
        return cache.get(file);
    }

    private final int hashCode;

    public NSObjectPathReference(final NSObject reference) {
        this.hashCode = reference.toString().hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof CacheReference) {
            return this.hashCode() == other.hashCode();
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NSObjectPathReference{");
        sb.append("hashCode=").append(hashCode);
        sb.append('}');
        return sb.toString();
    }
}