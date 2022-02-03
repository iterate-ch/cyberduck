package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoxFileidProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(BoxFileidProvider.class);

    public static final String ROOT = "0";

    private final BoxSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("fileid.cache.size"));

    public BoxFileidProvider(final BoxSession session) {
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
            return cached;
        }
        if(file.isRoot()) {
            return ROOT;
        }
        final Path f = new BoxListService(session, this).list(file.getParent(), listener).find(new SimplePathPredicate(file));
        if(null == f) {
            throw new NotfoundException(file.getAbsolute());
        }
        return f.attributes().getFileId();
    }

    public String cache(final Path file, final String id) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cache %s for file %s", id, file));
        }
        if(null == id) {
            cache.remove(new SimplePathPredicate(file));
            file.attributes().setFileId(null);
        }
        else {
            cache.put(new SimplePathPredicate(file), id);
            file.attributes().setFileId(id);
        }
        return id;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
