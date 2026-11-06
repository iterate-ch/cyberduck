package ch.cyberduck.core.cryptomator.impl.uvf;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.vault.VaultMetadataProvider;

import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;

/**
 * Provides the key encryption key and recovery key for a vault in Universal Vault Format. Parsing, validation
 * and serialization of vault.uvf is done by cryptolib.
 */
public interface UVFVaultMetadataProvider extends VaultMetadataProvider {

    /**
     * @param metadata Parsed vault.uvf
     * @return Unlocked vault
     */
    UvfUnlockedVault unlock(UvfLockedVault metadata) throws BackgroundException;

    /**
     * @return New vault with initial seed, key encryption key and recovery key
     */
    UvfUnlockedVault create() throws BackgroundException;

    static UVFVaultMetadataProvider cast(VaultMetadataProvider provider) throws ConnectionCanceledException {
        if(provider instanceof UVFVaultMetadataProvider) {
            return (UVFVaultMetadataProvider) provider;
        }
        else {
            throw new ConnectionCanceledException(new UnsupportedException(provider.getClass().getName()));
        }
    }
}
