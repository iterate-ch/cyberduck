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
import ch.cyberduck.core.vault.VaultMetadataProvider;

public interface VaultMetadataV8Provider extends VaultMetadataProvider {

    VaultCredentials getCredentials();

    static VaultMetadataV8Provider cast(VaultMetadataProvider provider) {
        if(provider instanceof VaultMetadataV8Provider) {
            return (VaultMetadataV8Provider) provider;
        }
        else {
            throw new IllegalArgumentException("Unsupported metadata type " + provider.getClass());
        }
    }
}
