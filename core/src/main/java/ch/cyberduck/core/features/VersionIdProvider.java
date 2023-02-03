package ch.cyberduck.core.features;

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
import ch.cyberduck.core.exception.BackgroundException;

/**
 * Determine an ID for a file for services where API calls take IDs rather than file paths. Implemented for services
 * where the file id changes when updating file contents.
 */
public interface VersionIdProvider {
    /**
     * Determine version id for file
     *
     * @param file File
     * @return Latest version id for file
     */
    String getVersionId(Path file) throws BackgroundException;

    /**
     * Clear any cached values
     */
    void clear();
}
