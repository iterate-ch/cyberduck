package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.ListFilteringFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CachingAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = LogManager.getLogger(CachingAttributesFinderFeature.class);

    private final Protocol.Case sensitivity;
    private final Cache<Path> cache;
    private final AttributesFinder delegate;

    /**
     * Use default feature looking up file using list feature
     *
     * @param cache Access from cache when available
     * @see DefaultAttributesFinderFeature
     */
    public CachingAttributesFinderFeature(final Session<?> session, final Cache<Path> cache) {
        this(session, cache, session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session)));
    }

    /**
     * @param cache    Access from cache when available
     * @param delegate Feature implementation
     */
    public CachingAttributesFinderFeature(final Session<?> session, final Cache<Path> cache, final AttributesFinder delegate) {
        this(session.getCaseSensitivity(), cache, delegate);
    }

    /**
     * @param sensitivity Case sensitivity for lookup in cache
     * @param cache       Access from cache when available
     * @param delegate    Feature implementation
     */
    public CachingAttributesFinderFeature(final Protocol.Case sensitivity, final Cache<Path> cache, final AttributesFinder delegate) {
        this.cache = cache;
        this.delegate = delegate;
        this.sensitivity = sensitivity;
    }

    /**
     * Return state from cached contents when available. Otherwise cache parent directory contents after lookup.
     */
    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        return this.find(file, new CachingListProgressListener(cache));
    }

    /**
     * Return state from cached contents when available. Invoke listener cleanup with directory contents when available
     *
     * @see ListProgressListener#cleanup(Path, AttributedList, Optional)
     */
    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return delegate.find(file, listener);
        }
        final Path directory = file.getParent();
        if(cache.isValid(directory)) {
            final AttributedList<Path> list = cache.get(directory);
            final Path found = list.find(new ListFilteringFeature.ListFilteringPredicate(sensitivity, file));
            if(null != found) {
                log.debug("Return cached attributes {} for {}", found.attributes(), file);
                return found.attributes();
            }
            log.debug("Cached directory listing does not contain {}", file);
            throw new NotfoundException(file.getAbsolute());
        }
        final MemoryListProgressListener memory = new MemoryListProgressListener(listener);
        try {
            final PathAttributes attr = delegate.find(file, memory);
            // Notify listener with contents
            memory.cleanup(directory, memory.getContents(), Optional.empty());
            return attr;
        }
        catch(NotfoundException e) {
            memory.cleanup(directory, memory.getContents(), Optional.of(e));
            throw e;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CachingAttributesFinderFeature{");
        sb.append("cache=").append(cache);
        sb.append(", delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
