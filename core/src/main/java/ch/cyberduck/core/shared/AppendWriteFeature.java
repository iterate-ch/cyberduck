package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;

public abstract class AppendWriteFeature implements Write {

    private Find finder;

    private Attributes attributes;

    protected AppendWriteFeature(final Session<?> session) {
        this.finder = new DefaultFindFeature(session);
        this.attributes = new DefaultAttributesFeature(session);
    }

    protected AppendWriteFeature(final Find finder, final Attributes attributes) {
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }
}
