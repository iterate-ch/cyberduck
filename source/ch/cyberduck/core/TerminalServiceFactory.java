package ch.cyberduck.core;

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

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class TerminalServiceFactory extends Factory<TerminalService> {

    private static final Map<Platform, TerminalServiceFactory> factories
            = new HashMap<Platform, TerminalServiceFactory>();

    public static void addFactory(Factory.Platform platform, TerminalServiceFactory f) {
        factories.put(platform, f);
    }

    public static TerminalService get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return null;
        }
        return factories.get(NATIVE_PLATFORM).create();
    }
}
