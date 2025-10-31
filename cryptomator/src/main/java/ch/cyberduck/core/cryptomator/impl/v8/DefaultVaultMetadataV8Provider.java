package ch.cyberduck.core.cryptomator.impl.v8;

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

import ch.cyberduck.core.vault.VaultCredentials;

public class DefaultVaultMetadataV8Provider implements VaultMetadataV8Provider {

    private VaultCredentials credentials;

    public DefaultVaultMetadataV8Provider(final VaultCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public VaultCredentials getCredentials() {
        return credentials;
    }
}
