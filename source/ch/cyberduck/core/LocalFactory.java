package ch.cyberduck.core;

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class LocalFactory extends Factory<Local> {

    /**
     * Registered factories
     */
    protected static final Map<Factory.Platform, LocalFactory> factories = new HashMap<Factory.Platform, LocalFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Factory.Platform platform, LocalFactory f) {
        factories.put(platform, f);
    }

    protected static LocalFactory getFactory() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
        }
        return factories.get(NATIVE_PLATFORM);
    }

    protected abstract Local create(Local parent, String name);

    public static Local createLocal(Local parent, String name) {
        return getFactory().create(parent, name);
    }

    protected abstract Local create(String parent, String name);

    public static Local createLocal(String parent, String name) {
        return getFactory().create(parent, name);
    }

    protected abstract Local create(String path);

    public static Local createLocal(String path) {
        return getFactory().create(path);
    }

    protected abstract Local create(File path);

    public static Local createLocal(File path) {
        return getFactory().create(path);
    }
}
