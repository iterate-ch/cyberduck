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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

/**
 * Retrieve metadata of file or folder from server
 */
@Required
public interface AttributesFinder {

    /**
     * Find file metadata
     *
     * @param file File or folder
     */
    default PathAttributes find(Path file) throws BackgroundException {
        return this.find(file, new DisabledListProgressListener());
    }

    /**
     * Find file metadata
     *
     * @param file     File or folder
     * @param listener Will optionally be invoked by implementation if directory listing is retrieved
     */
    PathAttributes find(Path file, ListProgressListener listener) throws BackgroundException;
}
