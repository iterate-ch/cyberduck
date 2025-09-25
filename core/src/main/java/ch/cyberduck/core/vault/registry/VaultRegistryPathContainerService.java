package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

public class VaultRegistryPathContainerService implements PathContainerService {

    private final Session<?> session;
    private final PathContainerService proxy;
    private final VaultRegistry registry;

    public VaultRegistryPathContainerService(final Session<?> session, final PathContainerService proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Path getRoot(final Path file) {
        try {
            return registry.find(session, file).getFeature(session, PathContainerService.class, proxy).getRoot(file);
        }
        catch(UnsupportedException | VaultUnlockCancelException e) {
            return proxy.getRoot(file);
        }
    }

    @Override
    public boolean isContainer(final Path file) {
        try {
            return registry.find(session, file).getFeature(session, PathContainerService.class, proxy).isContainer(file);
        }
        catch(UnsupportedException | VaultUnlockCancelException e) {
            return proxy.isContainer(file);
        }
    }

    @Override
    public Path getContainer(final Path file) {
        try {
            return registry.find(session, file).getFeature(session, PathContainerService.class, proxy).getContainer(file);
        }
        catch(UnsupportedException | VaultUnlockCancelException e) {
            return proxy.getContainer(file);
        }
    }

    @Override
    public String getKey(final Path file) {
        try {
            return registry.find(session, file).getFeature(session, PathContainerService.class, proxy).getKey(file);
        }
        catch(UnsupportedException | VaultUnlockCancelException e) {
            return proxy.getKey(file);
        }
    }
}
