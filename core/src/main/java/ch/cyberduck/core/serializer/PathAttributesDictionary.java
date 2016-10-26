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
import ch.cyberduck.core.PathAttributes;

import java.util.Collections;

public class PathAttributesDictionary {

    private final DeserializerFactory deserializer;

    public PathAttributesDictionary() {
        this.deserializer = new DeserializerFactory();
    }

    public PathAttributesDictionary(final DeserializerFactory deserializer) {
        this.deserializer = deserializer;
    }

    public <T> PathAttributes deserialize(T serialized) {
        final Deserializer dict = deserializer.create(serialized);
        final PathAttributes attributes = new PathAttributes();
        final String sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            attributes.setSize(Long.parseLong(sizeObj));
        }
        final String modifiedObj = dict.stringForKey("Modified");
        if(modifiedObj != null) {
            attributes.setModificationDate(Long.parseLong(modifiedObj));
        }
        final Object permissionObj = dict.objectForKey("Permission");
        if(permissionObj != null) {
            attributes.setPermission(new PermissionDictionary().deserialize(permissionObj));
        }
        attributes.setVersionId(dict.stringForKey("Version"));
        final String duplicateObj = dict.stringForKey("Duplicate");
        if(duplicateObj != null) {
            attributes.setDuplicate(Boolean.valueOf(duplicateObj));
        }
        attributes.setMetadata(Collections.<String, String>emptyMap());
        attributes.setRegion(dict.stringForKey("Region"));
        attributes.setStorageClass(dict.stringForKey("Storage Class"));
        return attributes;
    }
}