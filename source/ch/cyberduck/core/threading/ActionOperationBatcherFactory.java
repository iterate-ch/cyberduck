package ch.cyberduck.core.threading;

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

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class ActionOperationBatcherFactory extends Factory<ActionOperationBatcher> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, ActionOperationBatcherFactory> factories
            = new HashMap<Platform, ActionOperationBatcherFactory>();

    public static void addFactory(Platform platform, ActionOperationBatcherFactory f) {
        factories.put(platform, f);
    }

    public static ActionOperationBatcher get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return new ActionOperationBatcher() {
                public void operate() {
                    ;
                }
            };
        }
        return factories.get(NATIVE_PLATFORM).create();
    }
}
