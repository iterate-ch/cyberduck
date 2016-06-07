package ch.cyberduck.core.io.watchservice;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.concurrent.TimeUnit;

public class NIOEventWatchService implements RegisterWatchService {
    private static final Logger log = Logger.getLogger(NIOEventWatchService.class);

    private WatchService monitor;

    @Override
    public WatchKey register(final Watchable folder, final WatchEvent.Kind<?>[] events,
                             final WatchEvent.Modifier... modifiers) throws IOException {
        if(null == monitor) {
            monitor = FileSystems.getDefault().newWatchService();
        }
        final WatchKey key = folder.register(monitor, events, modifiers);
        if(log.isInfoEnabled()) {
            log.info(String.format("Registered for events for %s", key));
        }
        return key;
    }

    @Override
    public void release() throws IOException {
        this.close();
    }

    @Override
    public void close() throws IOException {
        monitor.close();
    }

    @Override
    public WatchKey poll() {
        return monitor.poll();
    }

    @Override
    public WatchKey poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return monitor.poll(timeout, unit);
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return monitor.take();
    }
}
