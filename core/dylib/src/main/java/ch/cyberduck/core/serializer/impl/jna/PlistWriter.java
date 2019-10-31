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
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.serializer.Writer;

import java.util.Collection;

public class PlistWriter<S extends Serializable> implements Writer<S> {

    @Override
    public void write(final Collection<S> collection, final Local file) throws AccessDeniedException {
        NSMutableArray list = NSMutableArray.array();
        for(S bookmark : collection) {
            list.addObject(bookmark.<NSDictionary>serialize(SerializerFactory.get()));
        }
        if(!list.writeToFile(file.getAbsolute())) {
            throw new LocalAccessDeniedException(String.format("Cannot create file %s", file.getAbsolute()));
        }
    }

    @Override
    public void write(final S item, final Local file) throws AccessDeniedException {
        if(!item.<NSDictionary>serialize(SerializerFactory.get()).writeToFile(file.getAbsolute())) {
            throw new LocalAccessDeniedException(String.format("Cannot create file %s", file.getAbsolute()));
        }
    }
}