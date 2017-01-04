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

public interface VaultRegistry {
    /**
     * @param file File
     * @return Vault for file or disabled vault if file is not inside a vault
     * @see Vault#DISABLED
     */
    Vault find(Path file);

    /**
     * Close and remove all vaults in registry
     */
    void clear();

    @SuppressWarnings("unchecked")
    <T> T getFeature(Session<?> session, Class<T> type, T proxy);

}
