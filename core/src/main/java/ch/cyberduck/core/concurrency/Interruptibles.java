package ch.cyberduck.core.concurrency;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Throwables;

import static java.lang.Thread.currentThread;

public class Interruptibles {
    private static final Logger log = LogManager.getLogger(Interruptibles.class);

    /**
     * Await count down and reset interrupt flag on thread prior throwing checked exception
     *
     * @param latch     Count down
     * @param throwable Checked exception type
     * @param <E>       Type of exception
     * @throws E Exception type when interrupted
     */
    public static <E extends Throwable> void await(final CountDownLatch latch, final Class<E> throwable) throws E {
        await(latch, throwable, CancelCallback.noop);
    }

    public static <E extends Throwable> void await(final CountDownLatch latch, Class<E> throwable, final CancelCallback cancel) throws E {
        try {
            if(cancel == CancelCallback.noop) {
                latch.await();
            }
            else {
                while(!latch.await(1, TimeUnit.SECONDS)) {
                    try {
                        cancel.verify();
                    }
                    catch(ConnectionCanceledException e) {
                        throw ExceptionUtils.throwableOfType(e, throwable);
                    }
                }
            }
        }
        catch(InterruptedException e) {
            final Thread thread = currentThread();
            if(log.isWarnEnabled()) {
                log.warn("Interrupted {} while waiting for {}", thread, latch);
            }
            thread.interrupt();
            throw ExceptionUtils.throwableOfType(e, throwable);
        }
    }

    public static <T> T await(final Future<T> future) throws BackgroundException {
        return await(future, CancelCallback.noop);
    }

    public static <T> T await(final Future<T> future,
                              final CancelCallback cancel) throws BackgroundException {
        try {
            if(cancel == CancelCallback.noop) {
                return future.get();
            }
            else {
                while(true) {
                    try {
                        return future.get(1L, TimeUnit.SECONDS);
                    }
                    catch(TimeoutException e) {
                        cancel.verify();
                    }
                }
            }
        }
        catch(ExecutionException e) {
            if(log.isWarnEnabled()) {
                log.warn("Execution of {} failed with {}", future, e);
            }
            for(Throwable cause : ExceptionUtils.getThrowableList(e)) {
                Throwables.throwIfInstanceOf(cause, BackgroundException.class);
            }
            throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
        }
        catch(InterruptedException e) {
            final Thread thread = currentThread();
            if(log.isWarnEnabled()) {
                log.warn("Interrupted {} while waiting for {}", thread, future);
            }
            thread.interrupt();
            throw ExceptionUtils.throwableOfType(e, ConnectionCanceledException.class);
        }
    }

    public static <T> List<T> awaitAll(final List<Future<T>> futures) throws BackgroundException {
        return awaitAll(futures, CancelCallback.noop);
    }

    public static <T> List<T> awaitAll(final List<Future<T>> futures,
                                       final CancelCallback cancel) throws BackgroundException {
        final List<T> results = new ArrayList<>();
        final AtomicReference<ConnectionCanceledException> canceled = new AtomicReference<>();
        for(Future<T> f : futures) {
            try {
                results.add(await(f, cancel));
            }
            catch(ConnectionCanceledException e) {
                canceled.set(e);
            }
        }
        if(canceled.get() != null) {
            throw canceled.get();
        }
        return results;
    }

    public static class ThreadAliveCancelCallback implements CancelCallback {
        private final Thread parent;

        public ThreadAliveCancelCallback() {
            this(Thread.currentThread());
        }

        public ThreadAliveCancelCallback(final Thread parent) {
            this.parent = parent;
        }

        @Override
        public void verify() throws ConnectionCanceledException {
            if(!parent.isAlive()) {
                if(log.isWarnEnabled()) {
                    log.warn("Cancel waiting with parent thread {} dead", parent);
                }
                throw new ConnectionCanceledException();
            }
        }
    }
}
