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
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

public abstract class OneTimeSchedulerFeature<R> implements Scheduler<Void> {

    private static final Logger log = Logger.getLogger(OneTimeSchedulerFeature.class);

    private final Path file;

    private final ThreadPool scheduler = ThreadPoolFactory.get("scheduler", 1);

    public OneTimeSchedulerFeature(final Path file) {
        this.file = file;
    }

    protected abstract R operate(PasswordCallback callback, Path file) throws BackgroundException;

    @Override
    public Void repeat(final PasswordCallback callback) {
        scheduler.execute(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return operate(callback, file);
            }
        });
        return null;
    }

    @Override
    public void shutdown() {
        scheduler.shutdown(false);
    }
}
