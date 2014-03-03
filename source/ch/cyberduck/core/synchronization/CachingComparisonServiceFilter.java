package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * @version $Id$
 */
public class CachingComparisonServiceFilter implements ComparePathFilter {
    private static final Logger log = Logger.getLogger(CachingComparisonServiceFilter.class);

    private Map<Path, Comparison> cache = Collections.<Path, Comparison>synchronizedMap(new LRUMap(
            Preferences.instance().getInteger("transfer.cache.size")));

    private ComparisionServiceFilter delegate;

    public CachingComparisonServiceFilter(final ComparisionServiceFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public Comparison compare(final Path file, final Local local) throws BackgroundException {
        if(!cache.containsKey(file)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Compare file %s", file));
            }
            cache.put(file, delegate.compare(file, local));
        }
        return cache.get(file);
    }

    public Comparison get(final Path file) {
        if(cache.containsKey(file)) {
            return cache.get(file);
        }
        return Comparison.notequal;
    }

    public void reset() {
        cache.clear();
    }
}
