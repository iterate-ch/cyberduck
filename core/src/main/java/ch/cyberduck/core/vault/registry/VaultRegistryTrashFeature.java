package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VaultRegistryTrashFeature implements Trash {

    private final Session<?> session;
    private final Trash proxy;
    private final VaultRegistry registry;

    public VaultRegistryTrashFeature(final Session<?> session, final Trash proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final Map<Vault, Map<Path, TransferStatus>> vaults = new HashMap<>();
        for(Map.Entry<Path, TransferStatus> file : files.entrySet()) {
            final Vault vault = registry.find(session, file.getKey());
            final Map<Path, TransferStatus> sorted;
            if(vaults.containsKey(vault)) {
                sorted = vaults.get(vault);
            }
            else {
                sorted = new LinkedHashMap<>();
            }
            sorted.put(file.getKey(), file.getValue());
            vaults.put(vault, sorted);
        }
        for(Map.Entry<Vault, Map<Path, TransferStatus>> entry : vaults.entrySet()) {
            final Vault vault = entry.getKey();
            final Trash feature = vault.getFeature(session, Trash.class, proxy);
            feature.delete(entry.getValue(), prompt, callback);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return proxy.isSupported(file);
    }

    @Override
    public boolean isRecursive() {
        return proxy.isRecursive();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryTrashFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
