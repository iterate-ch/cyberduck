package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

public class CachingFindFeature implements Find {

    private final Cache<Path> cache;
    private final Find delegate;

    public CachingFindFeature(final Cache<Path> cache, final Find delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(!file.isRoot()) {
            if(cache.isCached(file.getParent())) {
                final AttributedList<Path> list = cache.get(file.getParent());
                final Path found = list.find(new DefaultPathPredicate(file));
                return null != found;
            }
        }
        return delegate.find(file, new CachingListProgressListener(cache));
    }
}
