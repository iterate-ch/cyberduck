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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public class PathDictionary {

    public <T> Path deserialize(T serialized) {
        final Deserializer dict = DeserializerFactory.get(serialized);
        final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
        final String typeObj = dict.stringForKey("Type");
        if(typeObj != null) {
            for(String t : typeObj.replace("[", "").replace("]", "").split(", ")) {
                type.add(AbstractPath.Type.valueOf(t));
            }
        }
        final Path path;
        final Object attributesObj = dict.objectForKey("Attributes");
        if(attributesObj != null) {
            PathAttributes attributes = new PathAttributesDictionary().deserialize(attributesObj);
            // Legacy
            String legacyTypeObj = DeserializerFactory.get(attributesObj).stringForKey("Type");
            if(legacyTypeObj != null) {
                if((Integer.valueOf(legacyTypeObj) & AbstractPath.Type.file.legacy()) == AbstractPath.Type.file.legacy()) {
                    type.add(AbstractPath.Type.file);
                }
                if((Integer.valueOf(legacyTypeObj) & AbstractPath.Type.directory.legacy()) == AbstractPath.Type.directory.legacy()) {
                    type.add(AbstractPath.Type.directory);
                }
                if((Integer.valueOf(legacyTypeObj) & AbstractPath.Type.symboliclink.legacy()) == AbstractPath.Type.symboliclink.legacy()) {
                    type.add(AbstractPath.Type.symboliclink);
                }
                if((Integer.valueOf(legacyTypeObj) & AbstractPath.Type.volume.legacy()) == AbstractPath.Type.volume.legacy()) {
                    type.add(AbstractPath.Type.volume);
                }
            }
            if(type.isEmpty()) {
                return null;
            }
            path = new Path(dict.stringForKey("Remote"), type, attributes);
        }
        else {
            if(type.isEmpty()) {
                return null;
            }
            path = new Path(dict.stringForKey("Remote"), type);
        }
        final Object symlinkObj = dict.objectForKey("Symbolic Link");
        if(symlinkObj != null) {
            path.setSymlinkTarget(new PathDictionary().deserialize(symlinkObj));
        }
        return path;
    }
}
