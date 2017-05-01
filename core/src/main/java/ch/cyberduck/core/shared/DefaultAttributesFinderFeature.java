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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathPredicate;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.log4j.Logger;

public class DefaultAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(DefaultAttributesFinderFeature.class);

    private final Session<?> session;

    private Cache<Path> cache
            = PathCache.empty();

    public DefaultAttributesFinderFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        final AttributedList<Path> list;
        if(!cache.isCached(file.getParent())) {
            try {
                list = session.list(file.getParent(), new DisabledListProgressListener());
                cache.put(file.getParent(), list);
            }
            catch(InteroperabilityException | AccessDeniedException | NotfoundException f) {
                log.warn(String.format("Failure listing directory %s. %s", file.getParent(), f.getMessage()));
                // Try native implementation
                final AttributesFinder feature = session._getFeature(AttributesFinder.class);
                if(feature instanceof DefaultAttributesFinderFeature) {
                    throw f;
                }
                return feature.withCache(cache).find(file);
            }
        }
        else {
            list = cache.get(file.getParent());
        }
        final Path result = list.find(new PathPredicate(file));
        if(null == result) {
            throw new NotfoundException(file.getAbsolute());
        }
        return result.attributes();
    }

    @Override
    public DefaultAttributesFinderFeature withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
