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
import ch.cyberduck.core.preferences.HostPreferences;

public interface VaultRegistry {
    VaultRegistry DISABLED = new DisabledVaultRegistry();

    /**
     * @param session Connection
     * @param file    File
     * @return Vault for file or disabled vault if file is not inside a vault
     * @throws VaultUnlockCancelException Attempt to unlock vault was canceled by user
     * @see Vault#DISABLED
     */
    default Vault find(final Session session, final Path file) throws VaultUnlockCancelException {
        return this.find(session, file, new HostPreferences(session.getHost()).getBoolean("cryptomator.vault.autoload"));
    }

    /**
     * @param session Connection
     * @param file    File
     * @param unlock  Attempt to unlock vault when not already registered but file has vault referenced in attributes
     * @return Vault for file or disabled vault if file is not inside a vault
     * @throws VaultUnlockCancelException Attempt to unlock vault was canceled by user
     */
    Vault find(Session session, Path file, boolean unlock) throws VaultUnlockCancelException;

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

    /**
     * Wrap proxy with implementation looking up vault and wrap with cryptographic feature
     *
     * @param session Connection
     * @param type    Feature type
     * @param proxy   Proxy implementation to wrap with vault lookup
     * @return Feature implementation or null when not supported
     */
    @SuppressWarnings("unchecked")
    <T> T getFeature(Session<?> session, Class<T> type, T proxy);

    /**
     * @return True if directory is registered as vault or is contained in vault
     */
    boolean contains(Path directory);

    class DisabledVaultRegistry implements VaultRegistry {
        @Override
        public Vault find(final Session session, final Path file, final boolean unlock) throws VaultUnlockCancelException {
            return Vault.DISABLED;
        }

        @Override
        public void clear() {
            //
        }

        @Override
        public <T> T getFeature(final Session<?> session, final Class<T> type, final T proxy) {
            return proxy;
        }

        @Override
        public boolean contains(final Path vault) {
            return false;
        }

        @Override
        public boolean add(final Vault vault) {
            return false;
        }

        @Override
        public boolean close(final Path vault) {
            return false;
        }
    }
}
