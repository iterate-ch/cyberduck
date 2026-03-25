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

import com.nimbusds.jose.jwk.JWK;

public class DefaultVaultMetadataUVFProvider implements VaultMetadataUVFProvider {

    private final String vaultMetadata;
    private final JWK jwk;

    /**
     * Constructs a new instance of {@code DefaultVaultMetadataUVFProvider}.
     *
     * @param vaultMetadata         The metadata for the vault represented as a byte array.
     * @param jwk                   The {@code JWKCallback} instance used for key management and related operations.
     */
    public DefaultVaultMetadataUVFProvider(final String vaultMetadata, final JWK jwk) {
        this.vaultMetadata = vaultMetadata;
        this.jwk = jwk;
    }

    @Override
    public String getVaultMetadata() {
        return vaultMetadata;
    }

    @Override
    public JWK getKey() {
        return jwk;
    }
}
