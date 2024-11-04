package ch.cyberduck.core.preferences;

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

import ch.cyberduck.core.LocalFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PreferencesFactory {
    private static final Logger log = LogManager.getLogger(PreferencesFactory.class);

    private PreferencesFactory() {
        //
    }

    private static Preferences preferences;

    public static synchronized void set(final Preferences p) {
        preferences = p;
        preferences.load();
        preferences.setFactories();
        preferences.setDefaults();
        // Apply global configuration
        preferences.setDefaults(LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "default.properties"));
        preferences.configureLogging(preferences.getProperty("logging"));
        final Logger log = LogManager.getLogger(PreferencesFactory.class);
        log.info("Running version {}", preferences.getVersion());
    }

    public static synchronized Preferences get() {
        if(null == preferences) {
            log.error("No application preferences registered");
            set(new MemoryPreferences());
        }
        return preferences;
    }
}
