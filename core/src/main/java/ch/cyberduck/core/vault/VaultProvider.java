package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;

public interface VaultProvider {

    /**
     * Retrieves the metadata of the vault located at the specified path.
     *
     * @param path The path to the vault for which the metadata is to be retrieved.
     * @return Describes the properties of the vault at the given path. Null when no vault is found.
     */
    VaultMetadata metadata(Path path);

    /**
     * Searches for vault metadata in the specified directory using the given criteria.
     *
     * @param directory The path to the directory to search for vault metadata.
     * @param find      The criteria or parameters used to locate the vault metadata.
     * @param listener  A listener to report progress during the search operation.
     * @return The vault metadata found in the specified directory based on the provided criteria.
     * @throws BackgroundException If an error occurs during the search process.
     */
    VaultMetadata find(Path directory, Find find, ListProgressListener listener) throws BackgroundException;

    /**
     * Provides access to an existing vault based on the given session, directory, and metadata.
     *
     * @param session   The session used for connecting to the storage backend.
     * @param directory The path to the directory containing the vault.
     * @param metadata  The metadata describing the properties of the vault to be accessed.
     * @return An instance of the vault associated with the specified inputs.
     * @throws UnsupportedException If the vault cannot be provided due to unsupported conditions.
     */
    Vault provide(Session<?> session, Path directory, VaultMetadata metadata) throws UnsupportedException;

    /**
     * Creates a new vault in the specified directory with the provided credentials and metadata.
     *
     * @param session     The session used for connecting to the storage backend.
     * @param region      The region in which the vault should be created.
     * @param directory   The path to the directory where the vault will be created.
     * @param credentials The credentials required to authenticate and secure the vault.
     * @param metadata    The metadata describing the properties of the vault to be created.
     * @return An instance of the newly created vault.
     * @throws BackgroundException If an error occurs during the vault creation process.
     */
    Vault create(Session<?> session, String region, Path directory, VaultCredentials credentials, VaultMetadata metadata) throws BackgroundException;

    VaultProvider DISABLED = new DisabledVaultProvider();
}
