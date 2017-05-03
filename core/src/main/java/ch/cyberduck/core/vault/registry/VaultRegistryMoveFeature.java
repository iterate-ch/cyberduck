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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

import org.apache.log4j.Logger;

import java.util.Collections;

public class VaultRegistryMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(VaultRegistryMoveFeature.class);

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
            final Vault vault = registry.find(session, source);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Move %s to %s inside vault %s", source, target, vault));
            }
            // Move files inside vault
            vault.getFeature(session, Move.class, proxy).move(source, target, exists, callback);
        }
        else {
            // Move files from or into vault requires to pass through encryption features
            final Copy copy = session.getFeature(Copy.class);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Move %s to %s using copy feature %s", source, target, copy));
            }
            copy.copy(source, target, new TransferStatus());
            // Delete source file after copy is complete
            session.getFeature(Delete.class).delete(Collections.singletonList(source), new DisabledLoginCallback(), callback);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        try {
            if(registry.find(session, source).equals(registry.find(session, target))) {
                return registry.find(session, source, false).getFeature(session, Move.class, proxy).isRecursive(source, target);
            }
            return session.getFeature(Copy.class).isRecursive(source, target);
        }
        catch(VaultUnlockCancelException e) {
            return proxy.isRecursive(source, target);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        // Run through registry without looking for vaults to circumvent deadlock due to synchronized load of vault
        try {
            if(registry.find(session, source).equals(registry.find(session, target))) {
                return registry.find(session, source, false).getFeature(session, Move.class, proxy).isSupported(source, target);
            }
            return session.getFeature(Copy.class).isSupported(source, target);
        }
        catch(VaultUnlockCancelException e) {
            return false;
        }
    }

    @Override
    public Move withDelete(final Delete delete) {
        proxy.withDelete(delete);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryMoveFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
