package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class GraphFileIdProvider implements FileIdProvider {
    private static final Logger log = Logger.getLogger(GraphFileIdProvider.class);

    private final GraphSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("browser.cache.size"));

    public GraphFileIdProvider(final GraphSession session) {
        this.session = session;
    }

    @Override
    public String getFileId(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        if(cache.contains(new SimplePathPredicate(file))) {
            final String cached = cache.get(new SimplePathPredicate(file));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached fileid %s for file %s", cached, file));
            }
        }
        final AttributedList<Path> list = session._getFeature(ListService.class).list(file.getParent(), listener);
        final Path found = list.find(path -> file.getAbsolute().equals(path.getAbsolute()));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return this.cache(file, found.attributes().getFileId());
    }

    @Override
    public String cache(final Path file, final String id) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cache %s for file %s", id, file));
        }
        cache.put(new SimplePathPredicate(file), id);
        return id;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
