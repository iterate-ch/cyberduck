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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ProfileDictionary {
    private static final Logger log = Logger.getLogger(ProfileDictionary.class);

    private final DeserializerFactory deserializer;
    private final ProtocolFactory protocols;

    public ProfileDictionary() {
        this(ProtocolFactory.global);
    }

    public ProfileDictionary(final ProtocolFactory protocols) {
        this(protocols, new DeserializerFactory());
    }

    public ProfileDictionary(final DeserializerFactory deserializer) {
        this(ProtocolFactory.global, deserializer);
    }

    public ProfileDictionary(final ProtocolFactory protocols, final DeserializerFactory deserializer) {
        this.protocols = protocols;
        this.deserializer = deserializer;
    }

    public Profile deserialize(Object serialized) {
        final Deserializer<String> dict = deserializer.create(serialized);
        final String protocol = dict.stringForKey("Protocol");
        if(StringUtils.isNotBlank(protocol)) {
            final Protocol parent = protocols.find(protocol);
            if(null == parent) {
                log.error(String.format("Unknown protocol %s in profile", protocol));
                return null;
            }
            return new Profile(parent, dict);
        }
        log.error("Missing protocol in profile");
        return null;
    }
}