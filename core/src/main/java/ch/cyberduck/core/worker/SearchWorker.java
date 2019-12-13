package ch.cyberduck.core.worker;

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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = Logger.getLogger(SearchWorker.class);

    private final Path directory;
    private final Filter<Path> filter;
    private final Cache<Path> cache;
    private final ListProgressListener listener;

    public SearchWorker(final Path directory, final Filter<Path> filter, final Cache<Path> cache, final ListProgressListener listener) {
        this.directory = directory;
        this.filter = filter;
        this.cache = cache;
        this.listener = listener;
    }

    @Override
    public AttributedList<Path> run(final Session<?> session) throws BackgroundException {
        // Run recursively
        final Search feature = session.getFeature(Search.class).withCache(cache);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run with feature %s", feature));
        }
        return this.search(feature, directory);
    }

    private AttributedList<Path> search(final Search search, final Path workdir) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        // Get filtered list from search
        final AttributedList<Path> list = search.search(workdir, new RecursiveSearchFilter(filter), new WorkerListProgressListener(this, listener));
        if(!search.isRecursive()) {
            final Set<Path> removal = new HashSet<>();
            for(final Path file : list) {
                if(file.isDirectory()) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Recursively search in %s", file));
                    }
                    final AttributedList<Path> children = this.search(search, file);
                    list.addAll(children);
                    if(children.isEmpty()) {
                        removal.add(file);
                    }
                }
            }
            list.removeAll(removal);
        }
        return list;
    }

    @Override
    public AttributedList<Path> initialize() {
        return AttributedList.emptyList();
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Searching in {0}", "Status"), directory.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof SearchWorker)) {
            return false;
        }
        final SearchWorker that = (SearchWorker) o;
        return Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchWorker{");
        sb.append("filter='").append(filter).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private static final class RecursiveSearchFilter implements Filter<Path> {
        private final Filter<Path> filter;

        public RecursiveSearchFilter(final Filter<Path> filter) {
            this.filter = filter;
        }

        @Override
        public boolean accept(final Path file) {
            if(file.isDirectory()) {
                return true;
            }
            return filter.accept(file);
        }

        @Override
        public Pattern toPattern() {
            return filter.toPattern();
        }

        @Override
        public int hashCode() {
            return filter.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj instanceof Filter) {
                return filter.equals(obj);
            }
            return false;
        }
    }
}
