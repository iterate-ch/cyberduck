package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class ApplicationBadgeLabelerFactory extends Factory<ApplicationBadgeLabeler> {
    private static final Logger log = Logger.getLogger(ApplicationBadgeLabelerFactory.class);

    /**
     * Registered factories
     */
    private static final Map<Factory.Platform, ApplicationBadgeLabelerFactory> factories
            = new HashMap<Factory.Platform, ApplicationBadgeLabelerFactory>();

    public static void addFactory(Factory.Platform platform, ApplicationBadgeLabelerFactory f) {
        factories.put(platform, f);
    }

    public static ApplicationBadgeLabeler get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            log.warn(String.format("No implementation for %s", NATIVE_PLATFORM));
            return new DisabledApplicationBadgeLabeler();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }

    private static final class DisabledApplicationBadgeLabeler implements ApplicationBadgeLabeler {
        @Override
        public void badge(String label) {
            //
        }
    }
}