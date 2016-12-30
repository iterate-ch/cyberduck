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

import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultFinderBulkService;
import ch.cyberduck.core.vault.VaultLookupListener;

import java.util.Map;

public class VaultRegistryBulkFeature<R> implements Bulk<R> {

    private final DefaultVaultRegistry registry;
    private final VaultLookupListener lookup;
    private final PasswordStore keychain;
    private final Session<?> session;
    private final Bulk<R> proxy;

    public VaultRegistryBulkFeature(final Session<?> session, final Bulk<R> proxy, final DefaultVaultRegistry registry, final VaultLookupListener lookup, final PasswordStore keychain) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
        this.lookup = lookup;
        this.keychain = keychain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        for(Path file : files.keySet()) {
            final Vault vault = registry.find(file);
            if(vault.contains(file)) {
                return (R) vault.getFeature(session, Bulk.class, proxy).pre(type, files);
            }
        }
        return (R) new VaultFinderBulkService(proxy, keychain, lookup).pre(type, files);
    }
}
