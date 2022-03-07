package ch.cyberduck.core.local;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

public interface TemporaryFileService {
    /**
     * Derive temporary file name on disk with default prefix. The file is not touched.
     *
     * @param file File to derive temporary filename from
     * @return Path in temporary directory
     */
    Local create(Path file);

    /**
     * Derive temporary file name on disk with custom prefix. The file is not touched.
     *
     * @param uid  Folder name prefix
     * @param file File to derive temporary filename from
     * @return Path in temporary directory
     */
    Local create(String uid, Path file);

    /**
     * Derive temporary file name on disk.
     *
     * @param name Filename
     * @return Path in temporary directory
     */
    Local create(String name);

    /**
     * Delete temporary files
     */
    void shutdown();
}
