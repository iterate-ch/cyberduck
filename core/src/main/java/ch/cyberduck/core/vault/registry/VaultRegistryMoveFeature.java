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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryMoveFeature implements Move {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final Move proxy;

    public VaultRegistryMoveFeature(final Session<?> session, final Move proxy, final DefaultVaultRegistry registry) {

        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        registry.find(file).getFeature(session, Move.class, proxy).move(file, renamed, exists, callback);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return registry.find(source).getFeature(session, Move.class, proxy).isSupported(source, target);
    }
}
