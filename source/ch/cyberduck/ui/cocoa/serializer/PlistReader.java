package ch.cyberduck.ui.cocoa.serializer;

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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.Rococoa;

/**
 * @version $Id$
 * @param <S>
 */
public abstract class PlistReader<S extends Serializable> implements Reader<S> {

    public Collection<S> readCollection(Local file) {
        final Collection<S> c = new Collection<S>();
        NSArray list = NSArray.arrayWithContentsOfFile(file.getAbsolute());
        final NSEnumerator i = list.objectEnumerator();
        NSObject next;
        while(((next = i.nextObject()) != null)) {
            final NSDictionary dict = Rococoa.cast(next, NSDictionary.class);
            c.add(this.deserialize(dict));
        }
        return c;
    }

    /**
     * @param file A valid bookmark dictionary
     * @return Null if the file cannot be deserialized
     */
    public S read(Local file) {
        NSDictionary dict = NSDictionary.dictionaryWithContentsOfFile(file.getAbsolute());
        return this.deserialize(dict);
    }

    /**
     *
     * @param dict
     * @return
     */
    public abstract S deserialize(NSDictionary dict);
}