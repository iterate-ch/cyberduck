package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.sftp.HostKeyController;
import ch.cyberduck.ui.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class HostKeyControllerFactory extends Factory<HostKeyController> {

    public abstract HostKeyController create(Controller c);

    public abstract HostKeyController create(Session s);

    /**
     * Registered factories
     */
    protected static final Map<Platform, HostKeyControllerFactory> factories
            = new HashMap<Platform, HostKeyControllerFactory>();

    /**
     * @return Login controller instance for the current platform.
     */
    public static HostKeyController instance(Session s) {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
        }
        return factories.get(NATIVE_PLATFORM).create(s);
    }

    /**
     * @return Login controller instance for the current platform.
     */
    public static HostKeyController instance(Controller c) {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
        }
        return factories.get(NATIVE_PLATFORM).create(c);
    }

    /**
     * @param f
     */
    public static void addFactory(Platform p, HostKeyControllerFactory f) {
        factories.put(p, f);
    }
}
