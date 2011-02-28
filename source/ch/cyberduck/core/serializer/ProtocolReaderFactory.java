package ch.cyberduck.core.serializer;

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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class ProtocolReaderFactory extends Factory<Reader<Profile>> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, ProtocolReaderFactory> factories = new HashMap<Platform, ProtocolReaderFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Platform platform, ProtocolReaderFactory f) {
        factories.put(platform, f);
    }

    private static Reader<Profile> instance;

    /**
     * @return
     */
    public static Reader<Profile> instance() {
        if(null == instance) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
            }
            instance = factories.get(NATIVE_PLATFORM).create();
        }
        return instance;
    }
}