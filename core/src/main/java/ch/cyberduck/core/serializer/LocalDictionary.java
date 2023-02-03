package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;

import org.apache.commons.lang3.StringUtils;

public class LocalDictionary<T> {

    private final DeserializerFactory<T> factory;

    public LocalDictionary() {
        this.factory = new DeserializerFactory<>();
    }

    public LocalDictionary(final DeserializerFactory<T> factory) {
        this.factory = factory;
    }

    public Local deserialize(T serialized) {
        return this.deserialize(factory.create(serialized));
    }

    public Local deserialize(final Deserializer<T> dict) {
        final String path = dict.stringForKey("Path");
        if(StringUtils.isBlank(path)) {
            return null;
        }
        final Local file = LocalFactory.get(path);
        if(StringUtils.isNotBlank(dict.stringForKey("Bookmark"))) {
            file.setBookmark(dict.stringForKey("Bookmark"));
        }
        return file;
    }
}
