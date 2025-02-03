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
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MemoryListProgressListener extends ProxyListProgressListener {
    private static final Logger log = LogManager.getLogger(MemoryListProgressListener.class);

    private final AtomicReference<AttributedList<Path>> contents = new AtomicReference<>(AttributedList.emptyList());

    public MemoryListProgressListener(final ListProgressListener... proxy) {
        super(proxy);
    }

    @Override
    public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
        super.chunk(directory, list);
        log.debug("Cache list {} for {}", list, directory);
        contents.set(list);
    }

    @Override
    public void cleanup(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) {
        super.cleanup(directory, list, e);
        log.debug("Clear cached contents for {}", directory);
        contents.set(AttributedList.emptyList());
    }

    /**
     * @return Cached directory listing
     */
    public AttributedList<Path> getContents() {
        final AttributedList<Path> list = contents.get();
        log.debug("Return cached contents {}", list);
        return list;
    }
}
