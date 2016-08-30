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

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.concurrent.TimeUnit;

public class DisabledWatchService implements RegisterWatchService {

    @Override
    public WatchKey register(final Watchable folder, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public void release() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public WatchKey poll() {
        return null;
    }

    @Override
    public WatchKey poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return null;
    }
}
