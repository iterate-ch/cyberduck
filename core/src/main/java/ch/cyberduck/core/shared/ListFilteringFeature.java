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
import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;

public abstract class ListFilteringFeature {

    private final Session<?> session;

    public ListFilteringFeature(final Session<?> session) {
        this.session = session;
    }

    /**
     * @param file Query
     * @return Null if not found
     */
    protected Path search(final Path file) throws BackgroundException {
        try {
            // Do not decrypt filenames to match with input
            final AttributedList<Path> list = session._getFeature(ListService.class).list(file.getParent(), new IndexedListProgressListener() {
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
            });
            // Try to match path only as the version might have changed in the meantime
            return list.find(new ListFilteringPredicate(session, file));
        }
        catch(FilterFoundException e) {
            // Matching file found
            return e.getFile();
        }
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
