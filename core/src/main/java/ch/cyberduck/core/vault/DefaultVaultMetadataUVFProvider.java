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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;

public class DefaultVaultMetadataUVFProvider implements VaultMetadataUVFProvider {

    private final byte[] vaultMetadata;
    private final byte[] rootDirectoryMetadata;
    private final String rootDirectoryIdHash;
    private final JWKCallback jwk;

    /**
     * Constructs a new instance of {@code DefaultVaultMetadataUVFProvider}.
     *
     * @param vaultMetadata         The metadata for the vault represented as a byte array.
     * @param rootDirectoryMetadata The metadata for the root directory represented as a byte array.
     * @param rootDirectoryIdHash   The hash identifier for the root directory as a string.
     * @param jwk                   The {@code JWKCallback} instance used for key management and related operations.
     */
    public DefaultVaultMetadataUVFProvider(final byte[] vaultMetadata, final byte[] rootDirectoryMetadata, final String rootDirectoryIdHash, final JWKCallback jwk) {
        this.vaultMetadata = vaultMetadata;
        this.rootDirectoryMetadata = rootDirectoryMetadata;
        this.rootDirectoryIdHash = rootDirectoryIdHash;
        this.jwk = jwk;
    }

    @Override
    public void close(final String input) {
        jwk.close(input);
    }

    @Override
    public JWKCredentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        return jwk.prompt(bookmark, title, reason, options);
    }

    @Override
    public byte[] getVaultMetadata() {
        return vaultMetadata;
    }

    @Override
    public byte[] getRootDirectoryMetadata() {
        return rootDirectoryMetadata;
    }

    @Override
    public String getRootDirectoryIdHash() {
        return rootDirectoryIdHash;
    }
}
