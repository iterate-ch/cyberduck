package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultFinderListProgressListener;
import ch.cyberduck.core.vault.VaultFinderListService;
import ch.cyberduck.core.vault.VaultLookupListener;

public class VaultRegistryListService implements ListService {
    private final DefaultVaultRegistry registry;
    private final VaultLookupListener lookup;
    private final PasswordStore keychain;
    private final Session<?> session;
    private final ListService proxy;

    public VaultRegistryListService(final Session<?> session, final ListService proxy, final DefaultVaultRegistry registry, final VaultLookupListener lookup, final PasswordStore keychain) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
        this.lookup = lookup;
        this.keychain = keychain;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final Vault vault = registry.find(directory);
        if(vault.contains(directory)) {
            return vault.getFeature(session, ListService.class, proxy).list(directory, listener);
        }
        return new VaultFinderListService(session, proxy, new VaultFinderListProgressListener(keychain, lookup)).list(directory, listener);
    }
}
