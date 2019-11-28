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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class PreferencesFactory {
    private PreferencesFactory() {
        //
    }

    private static Preferences preferences;

    public static synchronized void set(final Preferences p) {
        preferences = p;
        preferences.load();
        preferences.setFactories();
        preferences.setDefaults();
        preferences.setLogging();
        // Apply global configuration
        final Local defaults = LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "default.properties");
        if(defaults.exists()) {
            final Properties props = new Properties();
            try (final InputStream in = defaults.getInputStream()) {
                props.load(in);
            }
            catch(AccessDeniedException | IOException e) {
                // Ignore failure loading configuration
            }
            for(Map.Entry<Object, Object> entry : props.entrySet()) {
                preferences.setDefault(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    public static synchronized Preferences get() {
        if(null == preferences) {
            set(new DefaultLoggingMemoryPreferenes());
        }
        return preferences;
    }

    private static final class DefaultLoggingMemoryPreferenes extends MemoryPreferences {
        @Override
        protected void setLogging() {
            this.setDefault("logging.config", "log4j.xml");
            super.setLogging();
        }
    }
}
