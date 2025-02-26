package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;

import java.util.HashMap;
import java.util.Map;

public final class HostPreferencesFactory {

    private static final Map<Host, HostPreferences> preferences = new HashMap<>();

    private HostPreferencesFactory() {
        //
    }

    public static synchronized HostPreferences get(final Host host) {
        if(!preferences.containsKey(host)) {
            preferences.put(host, new HostPreferences(host));
        }
        return preferences.get(host);
    }
}
