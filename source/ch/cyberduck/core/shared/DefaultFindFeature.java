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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

/**
 * @version $Id$
 */
public class DefaultFindFeature implements Find {

    private Session session;

    private Cache cache;

    public DefaultFindFeature(final Session session) {
        this(session, Cache.empty());
    }

    public DefaultFindFeature(final Session session, final Cache cache) {
        this.session = session;
        this.cache = cache;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final AttributedList<Path> list;
            if(!cache.containsKey(file.getParent().getReference())) {
                list = session.list(file.getParent(), new DisabledListProgressListener());
                cache.put(file.getParent().getReference(), list);
            }
            else {
                list = cache.get(file.getParent().getReference());
            }
            return list.contains(file.getReference());
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
