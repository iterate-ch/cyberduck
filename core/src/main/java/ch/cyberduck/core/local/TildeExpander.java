package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

public class TildeExpander {

    private final Preferences preferences = PreferencesFactory.get();

    public String abbreviate(final String name) {
        if(StringUtils.startsWith(name, preferences.getProperty("local.user.home"))) {
            return Local.HOME + StringUtils.removeStart(name, preferences.getProperty("local.user.home"));
        }
        return name;
    }

    public String expand(final String name) {
        if(name.startsWith(Local.HOME)) {
            return preferences.getProperty("local.user.home") + StringUtils.substring(name, 1);
        }
        return name;
    }
}
