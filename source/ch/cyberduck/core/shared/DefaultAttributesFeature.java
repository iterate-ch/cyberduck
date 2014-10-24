package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;

/**
 * @version $Id$
 */
public class DefaultAttributesFeature implements Attributes {

    private Session<?> session;

    private Cache cache;

    public DefaultAttributesFeature(final Session session) {
        this(session, Cache.empty());
    }

    public DefaultAttributesFeature(final Session session, final Cache cache) {
        this.session = session;
        this.cache = cache;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        final AttributedList<Path> list;
        if(!cache.containsKey(file.getParent().getReference())) {
            list = session.list(file.getParent(), new DisabledListProgressListener());
            cache.put(file.getParent().getReference(), list);
        }
        else {
            list = cache.get(file.getParent().getReference());
        }
        if(list.contains(file.getReference())) {
            return list.get(file.getReference()).attributes();
        }
        throw new NotfoundException(file.getAbsolute());
    }

    public Attributes withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
