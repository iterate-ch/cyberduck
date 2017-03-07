package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Search;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchMode;
import com.dropbox.core.v2.files.SearchResult;

public class DropboxSearchFeature implements Search {
    private final DropboxSession session;

    private final DropboxListService listService;

    public DropboxSearchFeature(final DropboxSession session) {
        this.session = session;
        this.listService = new DropboxListService(this.session);
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> list = new AttributedList<>();
            long start = 0;
            SearchResult result;
            do {
                result = new DbxUserFilesRequests(session.getClient()).searchBuilder(workdir.isRoot() ? StringUtils.EMPTY : workdir.getAbsolute(), regex.toPattern().pattern())
                        .withMode(SearchMode.FILENAME).withStart(start).start();
                final List<SearchMatch> matches = result.getMatches();
                for(SearchMatch match : matches) {
                    list.add(listService.parse(workdir, match.getMetadata()));
                    listener.chunk(workdir, list);
                }
                start = result.getStart();
            }
            while(result.getMore());
            return list;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to read attributes of {0}", e, workdir);
        }
    }

    @Override
    public Search withCache(final Cache<Path> cache) {
        return this;
    }
}