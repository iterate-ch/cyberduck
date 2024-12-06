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

import java.util.EnumSet;

public interface CryptoDirectory {

    /**
     * Get encrypted filename for given clear text filename with id of parent encrypted directory.
     *
     * @param session     Connection
     * @param parent      Parent folder
     * @param filename    Clear text filename
     * @param type        File type
     * @return Encrypted filename
     */
    String toEncrypted(Session<?> session, Path parent, String filename, EnumSet<Path.Type> type) throws BackgroundException;

    /**
     * Get encrypted reference for clear text directory path.
     *
     * @param session   Connection
     * @param directory Clear text
     */
    Path toEncrypted(Session<?> session, Path directory) throws BackgroundException;

    /**
     * Remove from cache
     */
    void delete(Path directory);

    void destroy();

    byte[] getOrCreateDirectoryId(Session<?> session, Path directory) throws BackgroundException;

    byte[] createDirectoryId(final Path directory);
}
