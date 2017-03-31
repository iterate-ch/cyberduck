package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

public class DefaultSearchFeature implements Search {

    private final Session<?> session;

    private Cache<Path> cache
            = PathCache.empty();

    public DefaultSearchFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> filter, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list;
        if(!cache.isCached(workdir)) {
            list = session.list(workdir, new SearchListProgressListener(filter, listener)).filter(filter);
            cache.put(workdir, list);
        }
        else {
            list = cache.get(workdir).filter(filter);
        }
        listener.chunk(workdir, list);
        return list;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }

    @Override
    public Search withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    private static final class SearchListProgressListener implements ListProgressListener {
        private final Filter<Path> filter;
        private final ListProgressListener delegate;

        public SearchListProgressListener(final Filter<Path> filter, final ListProgressListener delegate) {
            this.filter = filter;
            this.delegate = delegate;
        }

        @Override
        public void chunk(final Path parent, final AttributedList<Path> list) throws ConnectionCanceledException {
            delegate.chunk(parent, list.filter(filter));
        }

        @Override
        public void message(final String message) {
            delegate.message(message);
        }
    }
}
