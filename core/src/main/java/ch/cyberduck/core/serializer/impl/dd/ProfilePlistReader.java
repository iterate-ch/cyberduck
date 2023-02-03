package ch.cyberduck.core.serializer.impl.dd;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.ProfileDictionary;

import java.util.function.Predicate;

import com.dd.plist.NSDictionary;

public class ProfilePlistReader extends PlistReader<Profile> {

    private final DeserializerFactory<NSDictionary> deserializer = new DeserializerFactory<>();
    private final ProtocolFactory protocols;
    private final Predicate<Protocol> filter;

    public ProfilePlistReader() {
        this(ProtocolFactory.get());
    }

    public ProfilePlistReader(final ProtocolFactory protocols) {
        this(protocols, protocol -> true);
    }

    public ProfilePlistReader(final ProtocolFactory protocols, final Predicate<Protocol> parent) {
        this.protocols = protocols;
        this.filter = parent;
    }

    @Override
    public Profile deserialize(final NSDictionary dict) {
        return new ProfileDictionary<>(protocols, deserializer).deserialize(dict, filter);
    }
}