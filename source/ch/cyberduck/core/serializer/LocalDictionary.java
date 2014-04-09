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
import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class LocalDictionary {
    private static final Logger log = Logger.getLogger(LocalDictionary.class);

    public <T> Local deserialize(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final String path = dict.stringForKey("Path");
        if(StringUtils.isBlank(path)) {
            log.warn("Missing path");
            return null;
        }
        final Local file = LocalFactory.createLocal(path);
        final String data = dict.stringForKey("Bookmark");
        if(StringUtils.isNotBlank(data)) {
            file.setBookmark(data);
        }
        return file;
    }
}
