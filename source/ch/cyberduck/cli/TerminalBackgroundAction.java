package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Id$
 */
public class TerminalBackgroundAction<T> extends WorkerBackgroundAction<T> {
    private static final Logger log = Logger.getLogger(TerminalBackgroundAction.class);

    private AtomicBoolean retry
            = new AtomicBoolean();

    public TerminalBackgroundAction(final TerminalController controller,
                                    final Session<?> session,
                                    final Cache<Path> cache,
                                    final Worker<T> worker) {
        super(controller, session, cache, worker);
    }

    public TerminalBackgroundAction<T> withRetry(final boolean enabled) {
        retry = new AtomicBoolean(enabled);
        return this;
    }

    @Override
    protected int retry() {
        if(retry.getAndSet(false)) {
            return 1;
        }
        return 0;
    }
}
