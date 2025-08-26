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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VaultRegistryAclPermissionFeature implements AclPermission {

    private final Session<?> session;
    private final AclPermission proxy;
    private final VaultRegistry registry;

    public VaultRegistryAclPermissionFeature(final Session<?> session, final AclPermission proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, AclPermission.class, proxy).getPermission(file);
    }

    @Override
    public void setPermission(final Path file, final TransferStatus status) throws BackgroundException {
        registry.find(session, file).getFeature(session, AclPermission.class, proxy).setPermission(file, status);
    }

    @Override
    public List<Acl.User> getAvailableAclUsers(final List<Path> files) {
        try {
            final Set<Acl.User> users = new HashSet<>();
            for(Path file : files) {
                users.addAll(registry.find(session, file).getFeature(session, AclPermission.class, proxy).getAvailableAclUsers(Collections.singletonList(file)));
            }
            return new ArrayList<>(users);
        }
        catch(VaultUnlockCancelException e) {
            return proxy.getAvailableAclUsers(files);
        }
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        try {
            final Set<Acl.Role> users = new HashSet<>();
            for(Path file : files) {
                users.addAll(registry.find(session, file).getFeature(session, AclPermission.class, proxy).getAvailableAclRoles(Collections.singletonList(file)));
            }
            return new ArrayList<>(users);
        }
        catch(VaultUnlockCancelException e) {
            return proxy.getAvailableAclRoles(files);
        }
    }

    @Override
    public Acl getDefault(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, AclPermission.class, proxy).getDefault(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryAclPermissionFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
