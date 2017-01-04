package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class VaultCredentials extends Credentials {

    public VaultCredentials() {
        // Disable save in keychain by default
        this.setSaved(PreferencesFactory.get().getBoolean("vault.keychain"));
    }

    public VaultCredentials(final String password) {
        super(null, password);
        // Disable save in keychain by default
        this.setSaved(PreferencesFactory.get().getBoolean("vault.keychain"));
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Passphrase", "Cryptomator");
    }
}
