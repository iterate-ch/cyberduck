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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Search;

import java.text.MessageFormat;
import java.util.Objects;

public class SearchWorker extends Worker<AttributedList<Path>> {

    private final Path directory;
    private final Filter<Path> filter;
    private final PathCache cache;
    private final ListProgressListener listener;

    public SearchWorker(final Path directory, final Filter<Path> filter, final PathCache cache, final ListProgressListener listener) {
        this.directory = directory;
        this.filter = filter;
        this.cache = cache;
        this.listener = listener;
    }

    @Override
    public AttributedList<Path> run(final Session<?> session) throws BackgroundException {
        // Run recursively
        final AttributedList<Path> result = new AttributedList<>();
        final Search search = session.getFeature(Search.class);
        search.withCache(cache);
        this.search(search, directory, result);
        return result;
    }

    private void search(final Search search, final Path workdir, final AttributedList<Path> result) throws BackgroundException {
        for(Path file : search.search(workdir, filter, listener)) {
            if(file.isFile()) {
                result.add(file);
            }
            if(file.isDirectory()) {
                this.search(search, file, result);
            }
        }
    }

    @Override
    public void cleanup(final AttributedList<Path> result) {
        // Cache directory listing
        cache.put(directory, result);
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
}
