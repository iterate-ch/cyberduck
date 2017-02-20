package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class DefaultFindFeature implements Find {
    private static final Logger log = Logger.getLogger(DefaultFindFeature.class);

    private final Session<?> session;

    private Cache<Path> cache
            = PathCache.empty();

    public DefaultFindFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final AttributedList<Path> list;
            if(!cache.isCached(file.getParent())) {
                list = session.getFeature(ListService.class).list(file.getParent(), new DisabledListProgressListener());
                cache.put(file.getParent(), list);
            }
            else {
                list = cache.get(file.getParent());
            }
            // List always contains decrypted files
            final Path decrypted;
            if(file.getType().contains(AbstractPath.Type.encrypted)) {
                decrypted = file.attributes().getDecrypted();
            }
            else {
                decrypted = file;
            }
            final boolean found = list.contains(decrypted);
            if(!found) {
                switch(session.getCase()) {
                    case insensitive:
                        // Find for all matching filenames ignoring case
                        for(Path f : list) {
                            if(!f.getType().equals(file.getType())) {
                                continue;
                            }
                            if(StringUtils.equalsIgnoreCase(f.getName(), decrypted.getName())) {
                                log.warn(String.format("Found matching file %s ignoring case", f));
                                return true;
                            }
                        }
                }
                if(null == file.attributes().getVersionId()) {
                    final IdProvider id = session.getFeature(IdProvider.class);
                    final String version = id.getFileid(file);
                    if(version != null) {
                        return true;
                    }
                }
            }
            return found;
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public DefaultFindFeature withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
