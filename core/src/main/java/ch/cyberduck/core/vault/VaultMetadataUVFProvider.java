package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;

public interface VaultMetadataUVFProvider extends VaultMetadataProvider, JWKCallback {

    /**
     * Retrieves the JWE metadata associated with the vault.
     *
     * @return A byte array representing the vault metadata. This could include
     * encrypted configuration data or other related information necessary
     * for accessing or managing the vault.
     */
    byte[] getVaultMetadata();

    /**
     * Retrieves the metadata associated with the root directory of a vault.
     *
     * @return A byte array containing the metadata of the root directory. This metadata
     * may include encrypted details, configuration information, or any other
     * data required for managing or accessing the root directory of the vault.
     */
    byte[] getRootDirectoryMetadata();

    /**
     * Retrieves the hash identifier of the root directory.
     * This hash can be used as a unique identifier for the root directory
     * within the vault's metadata.
     *
     * @return A string representing the hash of the root directory's identifier.
     */
    String getRootDirectoryIdHash();

    static VaultMetadataUVFProvider cast(VaultMetadataProvider provider) throws ConnectionCanceledException {
        if(provider instanceof VaultMetadataUVFProvider) {
            return (VaultMetadataUVFProvider) provider;
        }
        else {
            throw new ConnectionCanceledException(new UnsupportedException(provider.getClass().getName()));
        }
    }
}
