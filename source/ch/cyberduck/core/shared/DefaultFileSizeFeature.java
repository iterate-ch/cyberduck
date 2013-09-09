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
import ch.cyberduck.core.features.Size;

/**
 * @version $Id:$
 */
public class DefaultFileSizeFeature implements Size {

    private Session session;

    private Cache cache = new Cache(100);

    public DefaultFileSizeFeature(final Session session) {
        this.session = session;
    }

    @Override
    public Long getSize(final Path file) throws BackgroundException {
        if(!cache.containsKey(file.getReference())) {
            cache.put(file.getReference(), session.list(file.getParent(), new DisabledListProgressListener()));
        }
        final AttributedList<Path> list = cache.get(file.getReference());
        if(list.contains(file.getReference())) {
            return list.get(file.getReference()).attributes().getSize();
        }
        // File not found
        return 0L;
    }
}
