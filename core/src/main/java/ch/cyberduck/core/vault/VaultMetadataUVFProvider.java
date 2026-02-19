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

public interface VaultMetadataUVFProvider extends VaultMetadataCallbackProvider {

    byte[] getMetadata();

    byte[] getRootDirectoryMetadata();

    String getDirPath();

    static VaultMetadataUVFProvider cast(VaultMetadataProvider provider) throws ConnectionCanceledException {
        if(provider instanceof VaultMetadataUVFProvider) {
            return (VaultMetadataUVFProvider) provider;
        }
        else {
            throw new ConnectionCanceledException(new UnsupportedException(provider.getClass().getName()));
        }
    }
}
