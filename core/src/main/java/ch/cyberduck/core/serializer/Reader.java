package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.exception.AccessDeniedException;

import java.io.InputStream;

public interface Reader<S extends Serializable> {

    /**
     * Read the serialized item from the given file
     *
     * @param file A valid dictionary
     * @return Deserialized item
     * @throws AccessDeniedException If the file is not readable
     */
    S read(Local file) throws AccessDeniedException;

    /**
     * Read the serialized item from the given stream
     *
     * @param in A valid dictionary
     * @return Deserialized item
     * @throws AccessDeniedException If the file is not readable
     */
    S read(InputStream in) throws AccessDeniedException;
}
