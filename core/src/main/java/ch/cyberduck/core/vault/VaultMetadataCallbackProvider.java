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

import ch.cyberduck.core.PasswordCallback;

public interface VaultMetadataCallbackProvider extends VaultMetadataProvider {

    PasswordCallback getPasswordCallback();

    static VaultMetadataCallbackProvider cast(VaultMetadataProvider provider) {
        if(provider instanceof VaultMetadataCallbackProvider) {
            return (VaultMetadataCallbackProvider) provider;
        }
        return null;
    }
}
