package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;

/**
 * Determine an ID for a file for services where API calls take IDs rather than file paths. Implemented for services
 * where the file id remains constant when updating file contents.
 */
@Optional
public interface FileIdProvider {
    /**
     * Determine file id for file
     *
     * @param file File
     * @return Latest file id for file
     */
    String getFileId(Path file) throws BackgroundException;

    /**
     * Clear any cached values
     */
    default void clear() {
        // No-op
    }
}
