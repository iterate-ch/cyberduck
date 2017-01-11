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

import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class VaultRegistryFactory {

    public static VaultRegistry create(final PasswordCallback callback) {
        return create(PasswordStoreFactory.get(), callback);
    }

    public static VaultRegistry create(final HostPasswordStore keychain, final PasswordCallback callback) {
        return PreferencesFactory.get().getBoolean("cryptomator.enable") ?
                new DefaultVaultRegistry(keychain, callback) : VaultRegistry.DISABLED;
    }
}
