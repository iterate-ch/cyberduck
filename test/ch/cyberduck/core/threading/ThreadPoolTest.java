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

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id:$
 */
public class ThreadPoolTest {

    @Test(expected = RejectedExecutionException.class)
    public void testShutdown() throws Exception {
        ThreadPool p = new ThreadPool();
        p.shutdown();
        p.execute(new Runnable() {
            @Override
            public void run() {
                fail();
            }
        });
    }

    @Test
    public void testExecute() throws Exception {
        ThreadPool p = new ThreadPool();
        final Object r = new Object();
        assertEquals(r, p.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return r;
            }
        }).get());
    }
}
