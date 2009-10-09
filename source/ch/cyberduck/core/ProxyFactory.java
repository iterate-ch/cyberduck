package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class ProxyFactory extends Factory<Proxy> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, ProxyFactory> factories = new HashMap<Platform, ProxyFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Platform platform, ProxyFactory f) {
        factories.put(platform, f);
    }

    private static Proxy proxy;

    /**
     * @return
     */
    public static Proxy instance() {
        if(null == proxy) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
            }
            proxy = factories.get(NATIVE_PLATFORM).create();
        }
        return proxy;
    }
}
