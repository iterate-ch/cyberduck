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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

public class VaultRegistryCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryCopyFeature(final Session<?> session, final Copy proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(registry.find(session, source).equals(Vault.DISABLED)) {
            registry.find(session, target).getFeature(session, Copy.class, proxy).copy(source, target, status);
        }
        else if(registry.find(session, target).equals(Vault.DISABLED)) {
            registry.find(session, source).getFeature(session, Copy.class, proxy).copy(source, target, status);
        }
        else {
            // Move files inside vault. May use server side copy.
            proxy.copy(source, target, status);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        try {
            if(registry.find(session, source, false).equals(Vault.DISABLED)) {
                return registry.find(session, target, false).getFeature(session, Copy.class, proxy).isRecursive(source, target);
            }
            else if(registry.find(session, target, false).equals(Vault.DISABLED)) {
                return registry.find(session, source, false).getFeature(session, Copy.class, proxy).isRecursive(source, target);
            }
        }
        catch(VaultUnlockCancelException e) {
            // Ignore
        }
        return proxy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        try {
            if(registry.find(session, source, false).equals(Vault.DISABLED)) {
                return registry.find(session, target, false).getFeature(session, Copy.class, proxy).isSupported(source, target);
            }
            else if(registry.find(session, target, false).equals(Vault.DISABLED)) {
                return registry.find(session, source, false).getFeature(session, Copy.class, proxy).isSupported(source, target);
            }
        }
        catch(VaultUnlockCancelException e) {
            // Ignore
        }
        return proxy.isSupported(source, target);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryCopyFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
