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
 * Lock files for exclusive access
 *
 * @param <T>
 */
@Optional
public interface Lock<T> {
    /**
     * Lock file
     *
     * @param file File
     * @return Lock token
     */
    T lock(Path file) throws BackgroundException;

    /**
     * Release previously obtained lock
     *
     * @param file  File
     * @param token Lock token
     * @throws BackgroundException
     */
    void unlock(Path file, T token) throws BackgroundException;
}
