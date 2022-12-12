package ch.cyberduck.core.serializer.impl.jna;

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

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.serializer.Reader;

public abstract class PlistReader<S extends Serializable> implements Reader<S> {

    /**
     * @param file A valid bookmark dictionary
     * @return Null if the file cannot be deserialized
     * @throws AccessDeniedException If the file is not readable
     */
    @Override
    public S read(final Local file) throws AccessDeniedException {
        if(!file.exists()) {
            throw new LocalAccessDeniedException(file.getAbsolute());
        }
        if(!file.isFile()) {
            throw new LocalAccessDeniedException(file.getAbsolute());
        }
        NSDictionary dict = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        if(null == dict) {
            throw new AccessDeniedException(String.format("Failure parsing file %s", file.getName()));
        }
        return this.deserialize(dict);
    }

    public abstract S deserialize(NSDictionary dict);
}
