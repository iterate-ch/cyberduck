package ch.cyberduck.ui.growl;

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
 * @version $Id: GrowlFactory.java 5451 2009-10-09 08:34:10Z dkocher $
 */
public abstract class GrowlFactory extends Factory<Growl> {
    private static Logger log = Logger.getLogger(GrowlFactory.class);

    /**
     * Registered factories
     */
    protected static final Map<Platform, GrowlFactory> factories = new HashMap<Platform, GrowlFactory>();

    public static void addFactory(Platform platform, GrowlFactory f) {
        factories.put(platform, f);
    }

    public static Growl get() {
        if(factories.containsKey(VERSION_PLATFORM)) {
            return factories.get(VERSION_PLATFORM).create();
        }
        else {
            return new Disabled();
        }
    }

    private static final class Disabled extends Growl {
        @Override
        public void setup() {
            log.warn("Growl notifications disabled");
        }

        @Override
        public void notify(String title, String description) {
            if(log.isInfoEnabled()) {
                log.info(description);
            }
        }

        @Override
        public void notifyWithImage(String title, String description, String image) {
            if(log.isInfoEnabled()) {
                log.info(description);
            }
        }
    }
}
