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
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CachingListProgressListener extends ProxyListProgressListener {
    private static final Logger log = LogManager.getLogger(CachingListProgressListener.class);

    private final Cache<Path> cache;

    public CachingListProgressListener(final Cache<Path> cache, final ListProgressListener... proxy) {
        super(proxy);
        this.cache = cache;
    }

    @Override
    public void cleanup(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) {
        // Add enumerated contents to cache
        if(e.isPresent()) {
            if(e.get() instanceof NotfoundException) {
                // Parent directory not found
                log.debug("Cache empty contents for directory {} after failure {}", directory, e.get().toString());
                cache.put(directory, AttributedList.emptyList());
            }
            else {
                log.warn("Failure {} caching contents for {}", e.get().toString(), directory);
            }
        }
        else {
            if(!(AttributedList.<Path>emptyList() == list)) {
                log.debug("Cache contents for {}", directory);
                cache.put(directory, list);
            }
            else {
                log.warn("Skip caching directory listing for {}", directory);
            }
        }
        super.cleanup(directory, list, e);
    }
}
