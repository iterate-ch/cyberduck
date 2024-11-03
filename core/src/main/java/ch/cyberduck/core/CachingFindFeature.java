package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.ListFilteringFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CachingFindFeature implements Find {
    private static final Logger log = LogManager.getLogger(CachingFindFeature.class);

    private final Protocol.Case sensitivity;
    private final Cache<Path> cache;
    private final Find delegate;

    public CachingFindFeature(final Session<?> session, final Cache<Path> cache, final Find delegate) {
        this(session.getCaseSensitivity(), cache, delegate);
    }

    public CachingFindFeature(final Protocol.Case sensitivity, final Cache<Path> cache, final Find delegate) {
        this.cache = cache;
        this.delegate = delegate;
        this.sensitivity = sensitivity;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return delegate.find(file, listener);
        }
        if(cache.isValid(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.find(new ListFilteringFeature.ListFilteringPredicate(sensitivity, file));
            if(found != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Found {} in cache", file);
                }
                return true;
            }
            if(log.isDebugEnabled()) {
                log.debug("Cached directory listing does not contain {}", file);
            }
            return false;
        }
        final CachingListProgressListener caching = new CachingListProgressListener(cache);
        final boolean found = delegate.find(file, new ProxyListProgressListener(listener, caching));
        caching.cache();
        return found;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CachingFindFeature{");
        sb.append("cache=").append(cache);
        sb.append(", delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
