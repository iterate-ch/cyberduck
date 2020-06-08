package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;

public abstract class ListFilteringFeature {

    private final Session<?> session;

    private Cache<Path> cache = PathCache.empty();

    public ListFilteringFeature(final Session<?> session) {
        this.session = session;
    }

    /**
     * @param file Query
     * @return Null if not found
     */
    protected Path search(final Path file) throws BackgroundException {
        final AttributedList<Path> list;
        if(!cache.isCached(file.getParent())) {
            try {
                // Do not decrypt filenames to match with input
                list = session._getFeature(ListService.class).list(file.getParent(), PathCache.empty() == cache ? new IndexedListProgressListener() {
                    @Override
                    public void message(final String message) {
                        //
                    }

                    @Override
                    public void visit(final AttributedList<Path> list, final int index, final Path f) throws ListCanceledException {
                        if(new ListFilteringPredicate(session, file).test(f)) {
                            throw new FilterFoundException(list, f);
                        }
                    }
                } : new DisabledListProgressListener());
                // No match but cache directory listing
                cache.put(file.getParent(), list);
            }
            catch(FilterFoundException e) {
                // Matching file found
                return e.getFile();
            }
        }
        else {
            list = cache.get(file.getParent());
        }
        // Try to match path only as the version might have changed in the meantime
        return list.find(new ListFilteringPredicate(session, file));
    }

    public ListFilteringFeature withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    private static final class ListFilteringPredicate extends DefaultPathPredicate {
        private final Session<?> session;
        private final Path file;

        public ListFilteringPredicate(final Session<?> session, final Path file) {
            super(file);
            this.session = session;
            this.file = file;
        }

        @Override
        public boolean test(final Path f) {
            if(StringUtils.isNotBlank(f.attributes().getVersionId())) {
                // Search with specific version and region
                if(new DefaultPathPredicate(file).test(f)) {
                    return true;
                }
            }
            if(f.attributes().isDuplicate()) {
                // Filter previous versions and delete markers
                return false;
            }
            switch(session.getCaseSensitivity()) {
                case sensitive:
                    return new SimplePathPredicate(file).test(f);
                case insensitive:
                    return new CaseInsensitivePathPredicate(file).test(f);
            }
            return false;
        }
    }

    private static final class FilterFoundException extends ListCanceledException {
        private final Path file;

        public FilterFoundException(final AttributedList<Path> chunk, final Path file) {
            super(chunk);
            this.file = file;
        }

        @Override
        public Path getFile() {
            return file;
        }
    }
}
