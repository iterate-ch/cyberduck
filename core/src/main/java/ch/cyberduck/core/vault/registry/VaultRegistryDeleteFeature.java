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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VaultRegistryDeleteFeature implements Delete {

    private final Session<?> session;
    private final Delete proxy;
    private final VaultRegistry registry;

    public VaultRegistryDeleteFeature(final Session<?> session, final Delete proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final Map<Vault, List<Path>> vaults = new HashMap<>();
        for(Path file : files) {
            final Vault vault = registry.find(session, file);
            final List<Path> sorted;
            if(vaults.containsKey(vault)) {
                sorted = vaults.get(vault);
            }
            else {
                sorted = new ArrayList<>();
            }
            sorted.add(file);
            vaults.put(vault, sorted);
        }
        for(Map.Entry<Vault, List<Path>> entry : vaults.entrySet()) {
            final Vault vault = entry.getKey();
            final Delete feature = vault.getFeature(session, Delete.class, proxy);
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
        final StringBuilder sb = new StringBuilder("VaultRegistryDeleteFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
