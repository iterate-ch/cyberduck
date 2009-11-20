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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @version $Id:$
 */
public class ThreadPool implements Executor {

    private static ThreadPool instance;

    private static final Object lock = new Object();

    /**
     * @return The singleton instance of me.
     */
    public static ThreadPool instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new ThreadPool();
            }
            return instance;
        }
    }

    /**
     * Thread pool
     */
    private static ExecutorService pool;

    private ExecutorService getExecutorService() {
        if(null == pool || pool.isShutdown()) {
            pool = Executors.newCachedThreadPool();
        }
        return pool;
    }

    private Executor getExecutor() {
        return this.getExecutorService();
    }

    public void shutdown() {
        this.getExecutorService().shutdownNow();
    }

    public void execute(Runnable command) {
        this.getExecutor().execute(command);
    }
}
