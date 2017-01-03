package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultUnixPermissionFeature;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryUnixPermission extends DefaultUnixPermissionFeature {

    private final Session<?> session;
    private final DefaultVaultRegistry registry;
    private final UnixPermission proxy;

    public VaultRegistryUnixPermission(final Session<?> session, final UnixPermission proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        registry.find(file).getFeature(session, UnixPermission.class, proxy).setUnixOwner(file, owner);
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        registry.find(file).getFeature(session, UnixPermission.class, proxy).setUnixGroup(file, group);
    }

    @Override
    public Permission getUnixPermission(final Path file) throws BackgroundException {
        return registry.find(file).getFeature(session, UnixPermission.class, proxy).getUnixPermission(file);
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        registry.find(file).getFeature(session, UnixPermission.class, proxy).setUnixPermission(file, permission);
    }
}
