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

public interface FilesystemBookmarkResolver<B> {

    /**
     * @param file File outside of sandbox
     * @return Security scoped bookmark
     * @throws AccessDeniedException Failure resolving bookmark for file
     */
    String create(Local file) throws AccessDeniedException;

    /**
     * @param file        File outside of sandbox
     * @param interactive Allow prompt
     * @return Reference to file by bookmark
     * @throws AccessDeniedException Failure resolving bookmark for file
     */
    B resolve(Local file, final boolean interactive) throws AccessDeniedException;
}
