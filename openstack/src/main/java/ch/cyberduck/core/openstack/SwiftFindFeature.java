package ch.cyberduck.core.openstack;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class SwiftFindFeature implements Find {

    private final SwiftMetadataFeature feature;

    private PathCache cache;

    public SwiftFindFeature(final SwiftSession session) {
        this(new SwiftMetadataFeature(session));
    }

    public SwiftFindFeature(final SwiftMetadataFeature feature) {
        this.feature = feature;
        this.cache = PathCache.empty();
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        final AttributedList<Path> list;
        if(cache.containsKey(file.getParent())) {
            list = cache.get(file.getParent());
        }
        else {
            list = new AttributedList<Path>();
            cache.put(file.getParent(), list);
        }
        if(list.contains(file)) {
            // Previously found
            return true;
        }
        if(cache.isHidden(file)) {
            // Previously not found
            return false;
        }
        try {
            feature.getMetadata(file);
            list.add(file);
            return true;
        }
        catch(NotfoundException e) {
            list.attributes().addHidden(file);
            return false;
        }
    }

    @Override
    public Find withCache(final PathCache cache) {
        this.cache = cache;
        return this;
    }
}
