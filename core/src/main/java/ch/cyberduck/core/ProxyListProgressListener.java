package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.Optional;

public class ProxyListProgressListener implements ListProgressListener {

    private final ListProgressListener[] proxy;

    public ProxyListProgressListener(final ListProgressListener... proxy) {
        this.proxy = proxy;
    }

    @Override
    public void message(final String message) {
        for(ListProgressListener listener : proxy) {
            listener.message(message);
        }
    }

    @Override
    public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
        for(ListProgressListener listener : proxy) {
            listener.chunk(directory, list);
        }
    }

    @Override
    public void finish(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) {
        for(ListProgressListener listener : proxy) {
            listener.finish(directory, list, e);
        }
    }

    @Override
    public ListProgressListener reset() throws ConnectionCanceledException {
        for(ListProgressListener listener : proxy) {
            listener.reset();
        }
        return this;
    }
}
