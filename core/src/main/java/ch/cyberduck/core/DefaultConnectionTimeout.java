package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultConnectionTimeout implements ConnectionTimeout.MutableConnectionTimeout {
    private static final Logger log = LogManager.getLogger(DefaultConnectionTimeout.class);

    public final static int TIMEOUT_MIN = 10;
    public final static int TIMEOUT_MAX = 60;

    private final Preferences mutablePreferences;
    private final PreferencesReader preferences;

    public DefaultConnectionTimeout() {
        mutablePreferences = PreferencesFactory.get();
        preferences = mutablePreferences;
    }

    public DefaultConnectionTimeout(final PreferencesReader preferences) {
        this.preferences = preferences;
        mutablePreferences = null;
    }

    @Override
    public int getTimeout() {
        return clamp(preferences.getInteger(PREFERENCE_KEY));
    }

    @Override
    public void setTimeout(final int timeout) {
        if(mutablePreferences == null) {
            log.warn(String.format("Trying to set %s to %d on Host preferences.", PREFERENCE_KEY, timeout));
            return;
        }
        mutablePreferences.setProperty(PREFERENCE_KEY, clamp(timeout));
    }

    private static int clamp(int value) {
        if(value < TIMEOUT_MIN) {
            value = TIMEOUT_MIN;
        }
        if(value > TIMEOUT_MAX) {
            value = TIMEOUT_MAX;
        }
        return value;
    }
}
