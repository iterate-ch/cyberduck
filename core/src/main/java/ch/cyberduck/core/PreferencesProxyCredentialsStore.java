package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.Preferences;

public class PreferencesProxyCredentialsStore implements ProxyCredentialsStore {

    private final Preferences preferences;

    public PreferencesProxyCredentialsStore() {
        this(new MemoryPreferences());
    }

    public PreferencesProxyCredentialsStore(final Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public Credentials getCredentials(final String proxy) {
        return new Credentials(
            preferences.getProperty(String.format("proxy.credentials.username.%s", proxy)),
            preferences.getProperty(String.format("proxy.credentials.password.%s", proxy))
        );
    }

    @Override
    public void addCredentials(final String proxy, final String accountName, final String password) {
        preferences.setProperty(String.format("proxy.credentials.username.%s", proxy), accountName);
        preferences.setProperty(String.format("proxy.credentials.password.%s", proxy), password);
    }

    @Override
    public void deleteCredentials(final String proxy) {
        preferences.deleteProperty(String.format("proxy.credentials.username.%s", proxy));
        preferences.deleteProperty(String.format("proxy.credentials.password.%s", proxy));
    }
}
