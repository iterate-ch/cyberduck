package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

public class VaultRegistryVaultProvider implements VaultProvider {

    private final Session<?> session;
    private final VaultProvider proxy;
    private final VaultRegistry registry;

    public VaultRegistryVaultProvider(final Session<?> session, final VaultProvider proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public VaultMetadata matches(final Path file) {
        try {
            return registry.find(session, file).getFeature(session, VaultProvider.class, proxy).matches(file);
        }
        catch(UnsupportedException | VaultUnlockCancelException e) {
            return null;
        }
    }

    @Override
    public VaultMetadata find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
        return registry.find(session, directory).getFeature(session, VaultProvider.class, proxy).find(directory, find, listener);
    }

    @Override
    public Vault provide(final Session<?> session, final Path directory, final VaultMetadata metadata) throws UnsupportedException {
        try {
            return registry.find(session, directory).getFeature(session, VaultProvider.class, proxy).provide(session, directory, metadata);
        }
        catch(VaultUnlockCancelException e) {
            return Vault.DISABLED;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryVaultProvider{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
