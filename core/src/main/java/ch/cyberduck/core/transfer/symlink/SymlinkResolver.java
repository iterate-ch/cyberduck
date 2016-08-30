package ch.cyberduck.core.transfer.symlink;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;

public interface SymlinkResolver<T> {

    /**
     * @param file Symbolic link
     * @return True if the symbolic link target can be resolved to another transfer item
     */
    boolean resolve(T file) throws NotfoundException, LocalAccessDeniedException;

    String relativize(String base, String name);
}
