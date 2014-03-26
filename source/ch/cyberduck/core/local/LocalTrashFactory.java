package ch.cyberduck.core.local;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.local.features.Trash;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class LocalTrashFactory extends Factory<Trash> {

    /**
     * Registered factories
     */
    private static final Map<Platform, LocalTrashFactory> factories
            = new HashMap<Platform, LocalTrashFactory>();

    public static void addFactory(Factory.Platform platform, LocalTrashFactory f) {
        factories.put(platform, f);
    }

    public static Trash get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return new DefaultLocalTrashFeature();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }
}
