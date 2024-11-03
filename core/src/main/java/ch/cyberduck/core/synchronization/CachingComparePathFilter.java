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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class CachingComparePathFilter implements ComparePathFilter {
    private static final Logger log = LogManager.getLogger(CachingComparePathFilter.class);

    private Map<TransferItem, Comparison> cache = Collections.emptyMap();

    private final DefaultComparePathFilter delegate;

    public CachingComparePathFilter(final DefaultComparePathFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public Comparison compare(final Path file, final Local local, final ProgressListener listener) throws BackgroundException {
        if(!cache.containsKey(new TransferItem(file, local))) {
            log.debug("Compare file {}", file);
            cache.put(new TransferItem(file, local), delegate.compare(file, local, listener));
        }
        final Comparison comparison = cache.get(new TransferItem(file, local));
        log.debug("Return comparison {} for file {}", comparison, file);
        return comparison;
    }

    public Comparison get(final TransferItem item) {
        if(cache.containsKey(item)) {
            return cache.get(item);
        }
        return Comparison.unknown;
    }

    public void reset() {
        cache.clear();
    }

    @Override
    public CachingComparePathFilter withFinder(final Find finder) {
        delegate.withFinder(finder);
        return this;
    }

    @Override
    public CachingComparePathFilter withAttributes(final AttributesFinder attribute) {
        delegate.withAttributes(attribute);
        return this;
    }

    @Override
    public CachingComparePathFilter withCache(final Map<TransferItem, Comparison> cache) {
        this.cache = cache;
        return this;
    }
}
