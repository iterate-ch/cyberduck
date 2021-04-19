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
import ch.cyberduck.core.features.AttributesFinder;

public class CachingAttributesFinderFeature implements AttributesFinder {

    private final Cache<Path> cache;
    private final AttributesFinder delegate;

    public CachingAttributesFinderFeature(final Cache<Path> cache, final AttributesFinder delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.find(new SimplePathPredicate(file));
            if(null != found) {
                return found.attributes();
            }
        }
        final PathAttributes attributes = delegate.find(file);
        if(cache != PathCache.empty()) {
            cache.get(file.getParent()).add(file.withAttributes(attributes));
        }
        return attributes;
    }
}
