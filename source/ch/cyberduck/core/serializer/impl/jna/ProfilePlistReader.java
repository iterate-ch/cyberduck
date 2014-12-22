package ch.cyberduck.core.serializer.impl.jna;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.serializer.ProfileDictionary;
import ch.cyberduck.binding.foundation.NSDictionary;

/**
 * @version $Id$
 */
public class ProfilePlistReader extends PlistReader<Profile> {

    private DeserializerFactory deserializer;

    public ProfilePlistReader() {
        this.deserializer = new DeserializerFactory();
    }

    public ProfilePlistReader(final DeserializerFactory deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public Profile deserialize(final NSDictionary dict) {
        return new ProfileDictionary(deserializer).deserialize(dict);
    }
}