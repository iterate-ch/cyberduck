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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import java.util.Optional;

public class DefaultSearchFeature implements Search {

    private final Session<?> session;

    public DefaultSearchFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> filter, final ListProgressListener listener) throws BackgroundException {
        return session.getFeature(ListService.class).list(workdir, new SearchListProgressListener(filter, listener)).filter(filter);
    }

    private static final class SearchListProgressListener implements ListProgressListener {
        private final Filter<Path> filter;
        private final ListProgressListener delegate;

        public SearchListProgressListener(final Filter<Path> filter, final ListProgressListener delegate) {
            this.filter = filter;
            this.delegate = delegate;
        }

        @Override
        public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
            delegate.chunk(directory, list.filter(filter));
        }

        @Override
        public void finish(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) throws ConnectionCanceledException {
            delegate.finish(directory, list, e);
        }

        @Override
        public ListProgressListener reset() {
            return this;
        }

        @Override
        public void message(final String message) {
            delegate.message(message);
        }
    }
}
