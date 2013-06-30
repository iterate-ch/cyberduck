package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id$
 */
public class ThreadPool {

    private final ThreadFactory threadFactory
            = new NamedThreadFactory();

    private final ExecutorService pool
            = Executors.newCachedThreadPool(threadFactory);

    public void shutdown() {
        pool.shutdownNow();
    }

    /**
     * @param command Action to run in its own executor thread
     */
    public void execute(final Runnable command) {
        pool.execute(command);
    }

    /**
     * @param command Action to run in its own executor thread
     * @return Future result
     */
    public <T> Future<T> execute(final Callable<T> command) throws RejectedExecutionException {
        return pool.submit(command);
    }
}