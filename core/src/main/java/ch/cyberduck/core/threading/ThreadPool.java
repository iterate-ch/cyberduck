package ch.cyberduck.core.threading;

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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ThreadPool<T> {

    /**
     * Execute task when slot becomes available
     */
    Future<T> execute(Callable<T> command);

    /**
     * Await completion of all previously submitted tasks
     */
    void await() throws BackgroundException;

    /**
     * Shutdown pool and reject any further executions
     *
     * @param gracefully Wait for tasks to complete
     */
    void shutdown(boolean gracefully);
}
