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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static ch.cyberduck.core.threading.ThreadPool.DEFAULT_THREAD_NAME_PREFIX;

public class ThreadPoolFactory extends Factory<ThreadPool> {
    private static final Logger log = LogManager.getLogger(ThreadPoolFactory.class);

    private Constructor<? extends ThreadPool> constructor;

    public ThreadPoolFactory() {
        super("factory.threadpool.class");
    }

    public ThreadPoolFactory(final Class<? extends ThreadPool> clazz) {
        super(clazz);
    }

    /**
     * @param prefix   Thread name
     * @param size     Maximum pool size
     * @param priority Thread priority
     * @param queue    Queue with pending tasks
     * @param handler  Uncaught thread exception handler
     * @return Thread pool
     */
    protected ThreadPool create(final String prefix, final Integer size, final ThreadPool.Priority priority,
                                final BlockingQueue<Runnable> queue, final Thread.UncaughtExceptionHandler handler) {
        try {
            if(null == constructor) {
                constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz,
                        prefix.getClass(), size.getClass(), priority.getClass(), queue.getClass(), handler.getClass());
            }
            if(null == constructor) {
                log.warn("No matching constructor for parameter {}", handler.getClass());
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(prefix, size, priority, queue, handler);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private static ThreadPoolFactory singleton;

    public static synchronized ThreadPool get() {
        return get(new LoggingUncaughtExceptionHandler());
    }

    public static synchronized ThreadPool get(final Thread.UncaughtExceptionHandler handler) {
        return get(DEFAULT_THREAD_NAME_PREFIX, PreferencesFactory.get().getInteger("threading.pool.size.max"), handler);
    }

    public static synchronized ThreadPool get(final String prefix, final Thread.UncaughtExceptionHandler handler) {
        return get(prefix, PreferencesFactory.get().getInteger("threading.pool.size.max"), handler);
    }

    public static synchronized ThreadPool get(final int size) {
        return get(DEFAULT_THREAD_NAME_PREFIX, size);
    }

    public static synchronized ThreadPool get(final int size, final Thread.UncaughtExceptionHandler handler) {
        return get(ThreadPool.DEFAULT_THREAD_NAME_PREFIX, size, handler);
    }

    public static synchronized ThreadPool get(final String prefix, final int size) {
        return get(prefix, size, ThreadPool.Priority.norm);
    }

    public static synchronized ThreadPool get(final String prefix, final ThreadPool.Priority priority) {
        return get(prefix, PreferencesFactory.get().getInteger("threading.pool.size.max"), priority);
    }

    public static synchronized ThreadPool get(final String prefix, final ThreadPool.Priority priority, final Thread.UncaughtExceptionHandler handler) {
        return get(prefix, PreferencesFactory.get().getInteger("threading.pool.size.max"), priority, handler);
    }

    public static synchronized ThreadPool get(final String prefix, final int size, final ThreadPool.Priority priority) {
        return get(prefix, size, priority, new LoggingUncaughtExceptionHandler());
    }

    public static synchronized ThreadPool get(final String prefix, final int size, final Thread.UncaughtExceptionHandler handler) {
        return get(prefix, size, ThreadPool.Priority.norm, handler);
    }

    public static synchronized ThreadPool get(final String prefix, final int size, final ThreadPool.Priority priority, final Thread.UncaughtExceptionHandler handler) {
        return get(prefix, size, priority, new LinkedBlockingQueue<>(size), handler);
    }

    public static synchronized ThreadPool get(final String prefix, final int size, final ThreadPool.Priority priority, final BlockingQueue<Runnable> queue) {
        return get(prefix, size, priority, queue, new LoggingUncaughtExceptionHandler());
    }

    public static synchronized ThreadPool get(final String prefix, final int size, final ThreadPool.Priority priority, final BlockingQueue<Runnable> queue, final Thread.UncaughtExceptionHandler handler) {
        if(null == singleton) {
            singleton = new ThreadPoolFactory();
        }
        return singleton.create(prefix, size, priority, queue, handler);
    }
}
