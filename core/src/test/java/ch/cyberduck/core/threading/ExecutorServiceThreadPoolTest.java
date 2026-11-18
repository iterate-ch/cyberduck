package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecutorServiceThreadPoolTest {

    @Test
    public void testShutdownTimeoutNeverTerminates() {
        final StubExecutorService executor = new StubExecutorService(false);
        new ExecutorServiceThreadPool(executor) {
        }.shutdown(false, Duration.ofMillis(10L));
        assertTrue(executor.isShutdown());
        assertEquals(1, executor.awaits.get());
    }

    @Test
    public void testShutdownTimeoutTerminates() {
        final StubExecutorService executor = new StubExecutorService(true);
        new ExecutorServiceThreadPool(executor) {
        }.shutdown(true, Duration.ofSeconds(5L));
        assertTrue(executor.isShutdown());
        assertEquals(1, executor.awaits.get());
    }

    private static final class StubExecutorService extends AbstractExecutorService {
        private final boolean terminates;
        private final AtomicInteger awaits = new AtomicInteger();
        private volatile boolean shutdown;

        StubExecutorService(final boolean terminates) {
            this.terminates = terminates;
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown && terminates;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) {
            awaits.incrementAndGet();
            return terminates;
        }

        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    }
}
