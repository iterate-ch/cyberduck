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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

/**
 * @param <T> Serialized object type
 */
public class ProfileDictionary<T> {
    private static final Logger log = LogManager.getLogger(ProfileDictionary.class);

    private final DeserializerFactory<T> deserializer;
    private final ProtocolFactory protocols;

    public ProfileDictionary() {
        this(ProtocolFactory.get());
    }

    public ProfileDictionary(final ProtocolFactory protocols) {
        this(protocols, new DeserializerFactory<>());
    }

    public ProfileDictionary(final DeserializerFactory<T> deserializer) {
        this(ProtocolFactory.get(), deserializer);
    }

    public ProfileDictionary(final ProtocolFactory protocols, final DeserializerFactory<T> deserializer) {
        this.protocols = protocols;
        this.deserializer = deserializer;
    }

    /**
     * @param serialized Serialized bookmark
     * @param filter     Filter for parent protocol reference
     * @return Null if deserialization fails
     */
    public Profile deserialize(final T serialized, final Predicate<Protocol> filter) {
        final Deserializer<T> dict = deserializer.create(serialized);
        final String protocol = dict.stringForKey("Protocol");
        if(StringUtils.isNotBlank(protocol)) {
            // Return default registered protocol specification as parent
            Protocol parent = protocols.forName(protocols.find(filter), protocol, null);
            if(null == parent) {
                log.error("Unknown protocol {} in profile. Try fallback with no predicate in lookup", protocol);
                parent = protocols.forName(protocols.find(p -> true), protocol, null);
            }
            if(null == parent) {
                return null;
            }
            return new Profile(parent, dict);
        }
        log.error("Missing protocol in profile");
        return null;
    }
}
