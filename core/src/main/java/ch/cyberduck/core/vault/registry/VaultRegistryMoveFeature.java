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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import java.util.Collections;

public class VaultRegistryMoveFeature implements Move {

    private final Session<?> session;
    private final Move proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryMoveFeature(final Session<?> session, final Move proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void move(final Path source, final Path target, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        if(registry.find(session, source).equals(registry.find(session, target))) {
            // Move files inside vault
            registry.find(session, source).getFeature(session, Move.class, proxy).move(source, target, exists, callback);
        }
        else {
            // Move files from or into vault requires to pass through encryption features
            new VaultRegistryCopyFeature(session, session.getFeature(Copy.class), registry).copy(source, target, new TransferStatus());
            // Delete source file after copy is complete
            new VaultRegistryDeleteFeature(session, session.getFeature(Delete.class), registry).delete(Collections.singletonList(source), new DisabledLoginCallback(), callback);
        }
    }

    @Override
    public boolean isRecursive(final Path source) {
        try {
            return registry.find(session, source, false).getFeature(session, Move.class, proxy).isRecursive(source);
        }
        catch(ConnectionCanceledException e) {
            return proxy.isRecursive(source);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        // Run through registry without looking for vaults to circumvent deadlock due to synchronized load of vault
        try {
            return registry.find(session, source, false).getFeature(session, Move.class, proxy).isSupported(source, target);
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    @Override
    public Move withDelete(final Delete delete) {
        proxy.withDelete(delete);
        return this;
    }
}
