package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

public interface CryptoFilename {

    /**
     * Check if a filename can be encrypted without exceeding the length supported by the vault
     *
     * @param filename Clear text filename
     * @return True if the filename can be encrypted
     */
    boolean isValid(String filename);

    /**
     * Name to store an encrypted filename with. Name shortening is not supported and filenames exceeding the
     * threshold of the vault are rejected.
     *
     * @param session  Connection
     * @param filename Encrypted filename
     * @return Filename to store
     * @throws CryptoInvalidFilenameException If the filename exceeds the threshold
     */
    String deflate(Session<?> session, String filename) throws BackgroundException;

    /**
     * Clear filename cache
     */
    void destroy();
}
