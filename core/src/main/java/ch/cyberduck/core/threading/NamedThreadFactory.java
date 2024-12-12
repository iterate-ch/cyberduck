package ch.cyberduck.core.threading;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final Logger log = LogManager.getLogger(NamedThreadFactory.class);

    private final AtomicInteger threadNumber
        = new AtomicInteger(1);

    private final String name;
    private final ThreadPool.Priority priority;
    private final Thread.UncaughtExceptionHandler handler;

    public NamedThreadFactory(final String name) {
        this(name, new LoggingUncaughtExceptionHandler());
    }

    public NamedThreadFactory(final String name, final Thread.UncaughtExceptionHandler handler) {
        this(name, ThreadPool.Priority.norm, handler);
    }

    public NamedThreadFactory(final String name, final ThreadPool.Priority priority, final Thread.UncaughtExceptionHandler handler) {
        this.name = name;
        this.priority = priority;
        this.handler = handler;
    }

    @Override
    public Thread newThread(final Runnable action) {
        log.debug("Create thread for runnable {}", action);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                action.run();
                log.debug("Finished execution of runnable {}", action);
            }
        });
        thread.setDaemon(true);
        thread.setName(String.format("%s-%d", name, threadNumber.getAndIncrement()));
        thread.setPriority(priority.toInteger());
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NamedThreadFactory{");
        sb.append("threadNumber=").append(threadNumber);
        sb.append(", name='").append(name).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(", handler=").append(handler);
        sb.append('}');
        return sb.toString();
    }
}
