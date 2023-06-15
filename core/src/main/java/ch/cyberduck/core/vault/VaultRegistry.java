package ch.cyberduck.core.vault;

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
import ch.cyberduck.core.features.Vault;

public interface VaultRegistry {
    VaultRegistry DISABLED = new DisabledVaultRegistry();

    /**
     * @param session Connection
     * @param file    File
     * @return Vault for file or disabled vault if file is not inside a vault
     * @see Vault#DISABLED
     */
    default Vault find(final Session session, final Path file) throws VaultUnlockCancelException {
        return this.find(session, file, true);
    }

    Vault find(Session session, Path file, boolean lookup) throws VaultUnlockCancelException;

    /**
     * Add vault to registry
     *
     * @param vault Vault
     * @return True if not already previously registered
     */
    boolean add(Vault vault);

    /**
     * Lock and remove from registry
     *
     * @param vault Vault directory
     */
    boolean close(Path vault);

    /**
     * Close and remove all vaults in registry
     */
    void clear();

    @SuppressWarnings("unchecked")
    <T> T getFeature(Session<?> session, Class<T> type, T proxy);

    boolean contains(Path vault);
}
