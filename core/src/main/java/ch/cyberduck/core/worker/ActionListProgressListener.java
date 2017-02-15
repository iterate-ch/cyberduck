package ch.cyberduck.core.worker;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

public class ActionListProgressListener implements ListProgressListener {

    private final Worker worker;

    private final ProgressListener delegate;

    public ActionListProgressListener(final Worker worker, final ProgressListener delegate) {
        this.worker = worker;
        this.delegate = delegate;
    }

    @Override
    public void chunk(final Path parent, AttributedList<Path> list) throws ConnectionCanceledException {
        if(worker.isCanceled()) {
            throw new ListCanceledException(list);
        }
    }

    @Override
    public void finish(final AttributedList<Path> list) {
        //
    }

    @Override
    public void message(final String message) {
        delegate.message(message);
    }
}
