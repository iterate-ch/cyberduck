package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.features.Vault;

import java.util.EnumSet;

public class RegistryVaultLoader implements VaultLoader {

    private final VaultRegistry registry;
    private final VaultLoader proxy;

    public RegistryVaultLoader(final VaultRegistry registry, final PasswordCallback prompt) {
        this(registry, new PasswordVaultLoader(registry, prompt));
    }

    public RegistryVaultLoader(final VaultRegistry registry, final VaultLoader proxy) {
        this.registry = registry;
        this.proxy = proxy;
    }

    @Override
    public Vault load(final Session<?> session, final Path directory, final VaultVersion version) throws VaultUnlockCancelException {
        final Vault vault = proxy.load(session, directory, version);
        if(registry.add(vault)) {
            final EnumSet<Path.Type> type = directory.getType();
            type.add(Path.Type.vault);
            directory.setType(type);
            directory.attributes().setVaultVersion(vault.getVersion());
        }
        return vault;
    }
}
