package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.security.KeyStore;

public class PreferencesX509KeyManager extends KeychainX509KeyManager {

    private Preferences preferences
            = PreferencesFactory.get();

    public PreferencesX509KeyManager(final CertificateStore callback) {
        super(callback);
    }

    public PreferencesX509KeyManager(final CertificateStore callback, final KeyStore store) {
        super(callback, store);
    }

    @Override
    protected String find(Key key) {
        return preferences.getProperty(key.toString());
    }

    @Override
    protected String save(Key key, String alias) {
        preferences.setProperty(key.toString(), alias);
        return alias;
    }
}
