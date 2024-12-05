package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PasswordCallback;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import java.util.concurrent.Future;

@Optional
public interface Scheduler<R> {
    /**
     * Repeated execution on background thread
     */
    Future<R> repeat(PasswordCallback callback);

    /**
     * Single execution on background thread with no delay
     */
    Future<R> execute(PasswordCallback callback);

    /**
     * Shutdown thread pool
     */
    void shutdown(boolean gracefully);

    Scheduler noop = new Scheduler() {
        @Override
        public Future repeat(final PasswordCallback callback) {
            return ConcurrentUtils.constantFuture(null);
        }

        @Override
        public Future execute(final PasswordCallback callback) {
            return ConcurrentUtils.constantFuture(null);
        }

        @Override
        public void shutdown(final boolean gracefully) {
        //
        }
    };
}
