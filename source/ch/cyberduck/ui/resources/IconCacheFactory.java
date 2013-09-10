package ch.cyberduck.ui.resources;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
import ch.cyberduck.core.FactoryException;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class IconCacheFactory extends Factory<IconCache> {

    private static final Map<Platform, IconCacheFactory> factories
            = new HashMap<Platform, IconCacheFactory>();

    public static void addFactory(Factory.Platform platform, IconCacheFactory f) {
        factories.put(platform, f);
    }

    private static IconCache cache;

    public static <I> IconCache<I> get() {
        if(null == cache) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new FactoryException(String.format("No implementation for %s", NATIVE_PLATFORM));
            }
            cache = factories.get(NATIVE_PLATFORM).create();
        }
        return cache;
    }
}
