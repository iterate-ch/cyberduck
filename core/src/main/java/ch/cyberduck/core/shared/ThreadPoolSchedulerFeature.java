package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Scheduler;
import ch.cyberduck.core.threading.ScheduledThreadPool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ThreadPoolSchedulerFeature<R> implements Scheduler<R> {
    private static final Logger log = LogManager.getLogger(ThreadPoolSchedulerFeature.class);

    private final long period;
    private final ScheduledThreadPool scheduler = new ScheduledThreadPool();

    public ThreadPoolSchedulerFeature(final long period) {
        this.period = period;
    }

    @Override
    public Future<R> repeat(final PasswordCallback callback) {
        return (ScheduledFuture<R>) scheduler.repeat(new FailureAwareRunnable(callback), period, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<R> execute(final PasswordCallback callback) {
        return (ScheduledFuture<R>) scheduler.schedule(new FailureAwareRunnable(callback), 0L, TimeUnit.MILLISECONDS);
    }

    protected abstract R operate(PasswordCallback callback) throws BackgroundException;

    @Override
    public void shutdown(final boolean gracefully) {
        log.debug("Shutting down scheduler thread pool {}", this);
        scheduler.shutdown(gracefully);
    }

    private final class FailureAwareRunnable implements Runnable {
        private final PasswordCallback callback;

        public FailureAwareRunnable(final PasswordCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                ThreadPoolSchedulerFeature.this.operate(callback);
            }
            catch(BackgroundException e) {
                log.warn("Failure processing scheduled task. {}", e.getMessage(), e);
            }
            catch(Exception e) {
                log.error("Failure processing scheduled task {}", e.getMessage(), e);
                ThreadPoolSchedulerFeature.this.shutdown(false);
            }
        }
    }
}
