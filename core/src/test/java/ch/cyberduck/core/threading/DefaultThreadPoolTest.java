package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultThreadPoolTest {

    @Test(expected = RejectedExecutionException.class)
    public void testShutdown() throws Exception {
        final DefaultThreadPool p = new DefaultThreadPool(1);
        p.shutdown(true);
        p.execute(new Callable<Void>() {
            @Override
            public Void call() {
                fail();
                return null;
            }
        });
    }

    @Test
    public void testGracefulShutdown() throws Exception {
        final DefaultThreadPool pool = new DefaultThreadPool(Integer.MAX_VALUE);
        final AtomicInteger counter = new AtomicInteger(10);
        for(int i = 0; i < 10; i++) {
            pool.execute(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Thread.sleep(10L);
                    return counter.decrementAndGet();
                }
            });
        }
        pool.shutdown(true);
        assertEquals(0, counter.get());
    }

    @Test
    public void testExecute() throws Exception {
        final DefaultThreadPool p = new DefaultThreadPool(Integer.MAX_VALUE);
        final Object r = new Object();
        assertEquals(r, p.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return r;
            }
        }).get());
    }

    @Test
    public void testFifoOrderSingleThread() throws Exception {
        final DefaultThreadPool p = new DefaultThreadPool(1);
        final List<Future<Integer>> wait = new ArrayList<Future<Integer>>();
        final AtomicInteger counter = new AtomicInteger(0);
        for(int i = 0; i < 1000; i++) {
            wait.add(p.execute(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return counter.incrementAndGet();
                }
            }));
        }
        int i = 1;
        for(Future f : wait) {
            assertEquals(i++, f.get());
        }
        p.shutdown(true);
        assertEquals(1000, counter.get());
    }

    @Test
    public void testShutdownGracefully() throws Exception {
        final DefaultThreadPool p = new DefaultThreadPool(Integer.MAX_VALUE);
        final List<Future<Integer>> wait = new ArrayList<Future<Integer>>();
        final AtomicInteger counter = new AtomicInteger(0);
        for(int i = 0; i < 1000; i++) {
            wait.add(p.execute(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return counter.incrementAndGet();
                }
            }));
        }
        p.shutdown(true);
        assertEquals(1000, counter.get());
    }
}
