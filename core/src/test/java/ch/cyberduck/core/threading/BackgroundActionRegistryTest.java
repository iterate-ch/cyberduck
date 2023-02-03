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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BackgroundActionRegistryTest {

    @Test
    public void testGlobal() {
        assertSame(BackgroundActionRegistry.global(), BackgroundActionRegistry.global());
    }

    @Test
    public void testGetCurrent() throws Exception {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final CountDownLatch lock = new CountDownLatch(1);
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() {
                return null;
            }

            @Override
            public void finish() {
                super.finish();
                lock.countDown();
            }
        };
        assertTrue(r.add(action));
        action.finish();
        lock.await(1, TimeUnit.SECONDS);
        r.remove(action);
        assertFalse(r.contains(action));
        assertNull(r.getCurrent());
    }

    @Test
    public void testCancel() {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() {
                return null;
            }
        };
        assertTrue(r.add(action));
        action.cancel();
        r.remove(action);
        assertFalse(r.contains(action));
        assertNull(r.getCurrent());
    }

    @Test
    public void testAddThreaded() throws Exception {
        final BackgroundActionRegistry r = new BackgroundActionRegistry();
        int runs = 10;
        final CountDownLatch lock = new CountDownLatch(runs);
        for(int i = 0; i < runs; i++) {
            final AbstractBackgroundAction action = new AbstractBackgroundAction() {
                @Override
                public Object run() {
                    return null;
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertTrue(r.add(action));
                    r.start(action);
                    lock.countDown();
                }
            }).start();
        }
        lock.await();
        assertEquals(runs, r.size());
    }

    @Test
    public void testAddRemoveThreaded() throws Exception {
        final BackgroundActionRegistry r = new BackgroundActionRegistry();
        int runs = 10;
        final CountDownLatch lock = new CountDownLatch(runs);
        for(int i = 0; i < runs; i++) {
            final AbstractBackgroundAction action = new AbstractBackgroundAction() {
                @Override
                public Object run() {
                    return null;
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertTrue(r.add(action));
                    r.start(action);
                    r.stop(action);
                    lock.countDown();
                }
            }).start();
        }
        lock.await();
        assertEquals(0, r.size());
    }
}
