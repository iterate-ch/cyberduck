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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryUnixPermissionFeature implements UnixPermission {

    private final Session<?> session;
    private final UnixPermission proxy;
    private final VaultRegistry registry;

    public VaultRegistryUnixPermissionFeature(final Session<?> session, final UnixPermission proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        registry.find(session, file).getFeature(session, UnixPermission.class, proxy).setUnixOwner(file, owner);
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        registry.find(session, file).getFeature(session, UnixPermission.class, proxy).setUnixGroup(file, group);
    }

    @Override
    public Permission getUnixPermission(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, UnixPermission.class, proxy).getUnixPermission(file);
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        registry.find(session, file).getFeature(session, UnixPermission.class, proxy).setUnixPermission(file, permission);
    }

    @Override
    public Permission getDefault(final Local file) {
        return proxy.getDefault(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryUnixPermissionFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
