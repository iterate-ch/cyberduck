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
 * @version $Id$
 */
public abstract class QuarantineServiceFactory extends Factory<QuarantineService> {
    private static final Logger log = Logger.getLogger(QuarantineServiceFactory.class);

    /**
     * Registered factories
     */
    private static final Map<Factory.Platform, QuarantineServiceFactory> factories
            = new HashMap<Factory.Platform, QuarantineServiceFactory>();

    public static void addFactory(Factory.Platform platform, QuarantineServiceFactory f) {
        factories.put(platform, f);
    }

    public static QuarantineService get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            log.warn(String.format("No implementation for %s", NATIVE_PLATFORM));
            return new DisabledQuarantineService();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }

    private static final class DisabledQuarantineService implements QuarantineService {
        @Override
        public void setQuarantine(final Local file, final String originUrl, final String dataUrl) {
            //
        }

        @Override
        public void setWhereFrom(final Local file, final String dataUrl) {
            //
        }
    }
}
