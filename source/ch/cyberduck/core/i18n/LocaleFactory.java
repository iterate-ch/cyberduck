package ch.cyberduck.core.i18n;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class LocaleFactory extends Factory<Locale> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, LocaleFactory> factories
            = new HashMap<Platform, LocaleFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Platform platform, LocaleFactory f) {
        factories.put(platform, f);
    }

    private static Locale l;

    /**
     * @return Locale instance for the current platform.
     */
    public static Locale instance() {
        if(null == l) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
            }
            l = factories.get(NATIVE_PLATFORM).create();
        }
        return l;
    }
}
