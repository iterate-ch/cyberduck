package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class ThreadedDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(ThreadedDeleteFeature.class);

    private static final Object lock = new Object();

    private ThreadPool<Void> pool;

    protected Future<Void> submit(final Path file, final ThreadedDeleteFeature.Implementation feature) {
        synchronized(lock) {
            if(null == pool) {
                pool = new DefaultThreadPool<Void>(PreferencesFactory.get().getInteger("browser.delete.concurrency"), "delete");
            }
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s for delete", file));
        }
        return pool.execute(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                feature.delete(file);
                return null;
            }
        });
    }

    /**
     * Await and shutdown
     */
    protected void await() throws BackgroundException {
        synchronized(lock) {
            try {
                pool.await();
            }
            finally {
                pool.shutdown(true);
                pool = null;
            }
        }
    }

    public interface Implementation {
        void delete(Path file) throws BackgroundException;
    }
}
