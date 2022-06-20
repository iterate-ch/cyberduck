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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CachingListProgressListener extends DisabledListProgressListener {
    private static final Logger log = LogManager.getLogger(CachingListProgressListener.class);

    private final Cache<Path> cache;
    private final Map<Path, AttributedList<Path>> contents = new HashMap<>(1);

    public CachingListProgressListener(final Cache<Path> cache) {
        this.cache = cache;
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) {
        contents.put(folder, list);
    }

    /**
     * Add enumerated contents to cache
     */
    public void cache() {
        for(Map.Entry<Path, AttributedList<Path>> entry : contents.entrySet()) {
            final AttributedList<Path> list = entry.getValue();
            if(!(AttributedList.<Path>emptyList() == list)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Cache directory listing for %s", entry.getKey()));
                }
                cache.put(entry.getKey(), list);
            }
        }
    }
}
