package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class MantaFindFeature implements Find {

    private final MantaSession session;
    private Cache<Path> cache;

    public MantaFindFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {

        // TODO: the following block is duplicated in several other Find features?
        // BEGIN duplicate code cache usage
        if(file.isRoot()) {
            return true;
        }
        final AttributedList<Path> list;
        if(cache.isCached(file.getParent())) {
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
        // END duplicate code cache usage

        try {
            new MantaAttributesFinderFeature(session).find(file);
            return true; // successfully found attributes for file
        } catch (Exception e) {
            // TODO: not every exception means "file not found"
            return false;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
