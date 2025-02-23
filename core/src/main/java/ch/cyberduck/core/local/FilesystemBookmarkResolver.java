package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

/**
 * @param <Resolved> Resolved file path from bookmark data
 */
public interface FilesystemBookmarkResolver<Resolved> {

    /**
     * Retain access to file-system resources with security-scoped bookmark.
     *
     * @param file File outside of sandbox
     * @return File alias or null on failure resolving bookmark for file
     */
    default String create(Local file) {
        return this.create(file, false);
    }

    /**
     * @param file   File outside of sandbox
     * @param prompt Allow to prompt user interactively to select file to allow obtaining security scoped bookmark
     * @return File alias or null on failure resolving bookmark for file
     */
    String create(Local file, boolean prompt);

    /**
     * Resolve the security-scoped bookmark
     *
     * @param bookmark Security-scoped bookmark
     * @return Reference to file by bookmark
     * @throws AccessDeniedException Failure resolving file with bookmark
     */
    Resolved resolve(String bookmark) throws AccessDeniedException;
}
