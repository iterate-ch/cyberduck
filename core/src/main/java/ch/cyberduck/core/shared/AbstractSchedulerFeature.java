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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Scheduler;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSchedulerFeature<R> implements Scheduler {
    private static final Logger log = Logger.getLogger(AbstractSchedulerFeature.class);

    private final long period;
    private final ScheduledThreadPool scheduler = new ScheduledThreadPool();
    private final CountDownLatch exit = new CountDownLatch(1);

    public AbstractSchedulerFeature(final long period) {
        this.period = period;
    }

    protected abstract R operate(PasswordCallback callback, Path file) throws BackgroundException;

    @Override
    public void run(final PasswordCallback callback) throws BackgroundException {
        scheduler.repeat(() -> {
            try {
                this.operate(callback, null);
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure processing missing file keys. %s", e.getDetail()));
            }
        }, period, TimeUnit.MILLISECONDS);
        try {
            exit.await();
        }
        catch(InterruptedException e) {
            log.error(String.format("Error waiting for exit signal %s", e.getMessage()));
            throw new DefaultExceptionMappingService().map(e);
        }
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
        exit.countDown();
    }
}
