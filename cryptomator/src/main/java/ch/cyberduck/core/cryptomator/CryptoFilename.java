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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

public interface CryptoFilename {

    /**
     * Check if a filename is deflated due to its length
     *
     * @param filename Encrypted filename
     * @return true if deflated, false otherwise
     */
    boolean isDeflated(String filename);

    /**
     * Check if a filename can be encrypted
     *
     * @param filename Encrypted filename
     * @return true if filenamed can be encrypted, false otherwise
     */
    boolean isValid(String filename);

    /**
     * Inflate a deflated filename
     *
     * @param session   Connection
     * @param shortName Deflated filename
     * @return Inflated filename
     */
    String inflate(Session<?> session, String shortName) throws BackgroundException;

    /**
     * Deflate a filename if it exceeds a threshold
     *
     * @param session  Connection
     * @param filename Filename
     * @return Deflated filename
     */
    String deflate(Session<?> session, String filename) throws BackgroundException;

    /**
     * Resolve metadata file for a deflated filename
     *
     * @param filename Filename
     * @return Path of metadata file
     */
    Path resolve(String filename);

    /**
     * Invalidate cache
     *
     * @param filename Filename to remove from cache
     */
    void invalidate(String filename);

    /**
     * Clear filename cache
     */
    void destroy();
}
