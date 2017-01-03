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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VaultRegistryAclPermission implements AclPermission {

    private final Session<?> session;
    private final AclPermission proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryAclPermission(final Session<?> session, final AclPermission proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        return registry.find(file).getFeature(session, AclPermission.class, proxy).getPermission(file);
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        registry.find(file).getFeature(session, AclPermission.class, proxy).setPermission(file, acl);
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return proxy.getAvailableAclUsers();
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        final Set<Acl.Role> roles = new HashSet<>();
        for(Path file : files) {
            roles.addAll(registry.find(file).getFeature(session, AclPermission.class, proxy).getAvailableAclRoles(Collections.singletonList(file)));
        }
        return new ArrayList<>(roles);
    }

    @Override
    public Acl getDefault(final Local file) {
        return proxy.getDefault(file);
    }
}
