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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;

import java.util.Set;

public class DelegatingVaultLookupListener implements VaultLookupListener {

    private final Set<VaultLookupListener> listeners;

    public DelegatingVaultLookupListener(final Set<VaultLookupListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void found(final Vault vault) throws ConnectionCanceledException {
        for(VaultLookupListener l : listeners) {
            l.found(vault);
        }
    }
}
