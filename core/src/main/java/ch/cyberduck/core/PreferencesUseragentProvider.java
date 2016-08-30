package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class PreferencesUseragentProvider implements UseragentProvider {

    private static final Preferences preferences
            = PreferencesFactory.get();

    private static final String ua = String.format("%s/%s.%s (%s/%s) (%s)",
            preferences.getProperty("application.name"),
            preferences.getProperty("application.version"),
            preferences.getProperty("application.revision"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"));

    @Override
    public String get() {
        return ua;
    }
}
