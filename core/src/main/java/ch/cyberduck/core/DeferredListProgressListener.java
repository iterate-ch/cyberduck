package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

public final class DeferredListProgressListener implements ListProgressListener {
    private static final Logger log = Logger.getLogger(DeferredListProgressListener.class);

    private final Path directory;
    private final ListProgressListener proxy;
    /**
     * Retain chunk notification until list size reaches this minimum
     */
    private final Integer retain;

    public DeferredListProgressListener(final Path directory, final ListProgressListener proxy) {
        this(directory, proxy, 5);
    }

    public DeferredListProgressListener(final Path directory, final ListProgressListener proxy, final Integer retain) {
        this.directory = directory;
        this.proxy = proxy;
        this.retain = retain;
    }

    @Override
    public void message(final String message) {
        proxy.message(message);
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
        // Defer notification until we can be sure no vault is found
        if(list.size() < retain) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Delay chunk notification for file listing of folder %s", directory));
            }
            return;
        }
        proxy.chunk(folder, list);
    }

    @Override
    public void finish(final AttributedList<Path> list) {
        // Notify with chunk if delayed
        if(list.size() < retain) {
            if(!list.isEmpty()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Deferred chunk notification for file listing of folder %s", directory));
                }
                try {
                    proxy.chunk(directory, list);
                }
                catch(ConnectionCanceledException ignored) {
                    // Ignore
                }
            }
        }
        proxy.finish(list);
    }
}
