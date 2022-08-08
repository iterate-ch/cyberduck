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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class PathDictionary<T> {
    private static final Logger log = LogManager.getLogger(PathDictionary.class);

    private final DeserializerFactory<T> factory;

    public PathDictionary() {
        this.factory = new DeserializerFactory<>();
    }

    public PathDictionary(final DeserializerFactory<T> factory) {
        this.factory = factory;
    }

    public Path deserialize(final T serialized) {
        final Deserializer<T> dict = factory.create(serialized);
        final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
        final String typeObj = dict.stringForKey("Type");
        if(typeObj != null) {
            for(String t : StringUtils.splitByWholeSeparator(StringUtils.replaceEach(typeObj, new String[]{"[", "]"}, new String[]{"", ""}), ", ")) {
                try {
                    type.add(Path.Type.valueOf(t));
                }
                catch(IllegalArgumentException e) {
                    log.warn(String.format("Unknown type %s", t));
                }
            }
        }
        final Path path;
        final T attributesObj = dict.objectForKey("Attributes");
        if(attributesObj != null) {
            final PathAttributes attributes = new PathAttributesDictionary<>(factory).deserialize(attributesObj);
            // Legacy
            final String legacyTypeObj = factory.create(attributesObj).stringForKey("Type");
            if(legacyTypeObj != null) {
                if((Integer.parseInt(legacyTypeObj) & AbstractPath.Type.file.legacy()) == AbstractPath.Type.file.legacy()) {
                    type.add(AbstractPath.Type.file);
                }
                if((Integer.parseInt(legacyTypeObj) & AbstractPath.Type.directory.legacy()) == AbstractPath.Type.directory.legacy()) {
                    type.add(AbstractPath.Type.directory);
                }
                if((Integer.parseInt(legacyTypeObj) & AbstractPath.Type.symboliclink.legacy()) == AbstractPath.Type.symboliclink.legacy()) {
                    type.add(AbstractPath.Type.symboliclink);
                }
                if((Integer.parseInt(legacyTypeObj) & AbstractPath.Type.volume.legacy()) == AbstractPath.Type.volume.legacy()) {
                    type.add(AbstractPath.Type.volume);
                }
            }
            if(type.isEmpty()) {
                return null;
            }
            final String absolute = dict.stringForKey("Remote");
            if(null == absolute) {
                return null;
            }
            path = new Path(absolute, type, attributes);
        }
        else {
            if(type.isEmpty()) {
                return null;
            }
            final String absolute = dict.stringForKey("Remote");
            if(null == absolute) {
                return null;
            }
            path = new Path(absolute, type);
        }
        final T symlinkObj = dict.objectForKey("Symbolic Link");
        if(symlinkObj != null) {
            path.setSymlinkTarget(new PathDictionary<>(factory).deserialize(symlinkObj));
        }
        return path;
    }
}
