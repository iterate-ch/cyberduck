package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;

public class DisabledVaultProvider implements VaultProvider {

    @Override
    public VaultVersion matches(final Path file) {
        return null;
    }

    @Override
    public VaultVersion find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
        return null;
    }

    @Override
    public Vault load(final Session<?> session, final Path directory, final VaultVersion version, final VaultCredentials credentials) {
        return Vault.DISABLED;
    }

    @Override
    public Vault create(final Session<?> session, final String region, final Path directory, final VaultVersion version, final VaultCredentials credentials) throws BackgroundException {
        return Vault.DISABLED;
    }
}
