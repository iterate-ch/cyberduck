package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.features.Vault;

public class DisabledVaultRegistry implements VaultRegistry {

    @Override
    public Vault find(final Session session, final Path file, final boolean lookup) throws VaultUnlockCancelException {
        return Vault.DISABLED;
    }

    @Override
    public void clear() {
        //
    }

    @Override
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T proxy) {
        return proxy;
    }

    @Override
    public boolean contains(final Path vault) {
        return false;
    }

    @Override
    public boolean add(final Vault vault) {
        return false;
    }

    @Override
    public boolean close(final Path vault) {
        return false;
    }
}
