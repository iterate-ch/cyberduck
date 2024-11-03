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
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = LogManager.getLogger(SearchWorker.class);

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
        final Search feature = session.getFeature(Search.class);
        if(log.isDebugEnabled()) {
            log.debug("Run with feature {}", feature);
        }
        return this.search(feature, directory);
    }

    private AttributedList<Path> search(final Search search, final Path workdir) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        final AttributedList<Path> list;
        if(!search.isRecursive() && cache.isCached(workdir)) {
            list = new AttributedList<>(cache.get(workdir));
        }
        else {
            // Get filtered list from search
            list = search.search(workdir, new RecursiveSearchFilter(filter), new WorkerListProgressListener(this, listener));
            if(!search.isRecursive()) {
                cache.put(workdir, new AttributedList<>(list));
            }
        }
        final Set<Path> removal = new HashSet<>();
        if(search.isRecursive()) {
            for(Path directory : list) {
                if(directory.isDirectory()) {
                    if(!list.toStream().filter(f -> f.isChild(directory)).findAny().isPresent()) {
                        removal.add(directory);
                    }
                }
            }
        }
        else {
            for(final Path f : list) {
                if(f.isDirectory()) {
                    if(log.isDebugEnabled()) {
                        log.debug("Recursively search in {}", f);
                    }
                    final AttributedList<Path> children = this.search(search, f);
                    list.addAll(children);
                    if(children.isEmpty()) {
                        removal.add(f);
                    }
                }
            }
        }
        return list.filter(new NullFilter<Path>() {
            @Override
            public boolean accept(final Path file) {
                return !removal.contains(file);
            }
        }).filter(new RecursiveSearchFilter(filter));
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
        private final Filter<Path> proxy;

        public RecursiveSearchFilter(final Filter<Path> proxy) {
            this.proxy = proxy;
        }

        @Override
        public boolean accept(final Path file) {
            if(file.isDirectory()) {
                return true;
            }
            return proxy.accept(file);
        }

        @Override
        public Pattern toPattern() {
            return proxy.toPattern();
        }

        @Override
        public String toString() {
            return proxy.toString();
        }

        @Override
        public int hashCode() {
            return proxy.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj instanceof Filter) {
                return proxy.equals(obj);
            }
            return false;
        }
    }
}
