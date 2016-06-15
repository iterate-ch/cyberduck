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

import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BackgroundActionRegistryTest {

    @Test
    public void testGlobal() throws Exception {
        assertSame(BackgroundActionRegistry.global(), BackgroundActionRegistry.global());
    }

    @Test
    public void testGetCurrent() throws Exception {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final CountDownLatch lock = new CountDownLatch(1);
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
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
    public void testCancel() throws Exception {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
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
    public void testAddSecondaryThread() throws Exception {
        final BackgroundActionRegistry r = new BackgroundActionRegistry();
        final CountDownLatch lock = new CountDownLatch(1);
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
                return null;
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                r.add(action);
                lock.countDown();
            }
        }).start();
        lock.await(1, TimeUnit.SECONDS);
        assertTrue(r.size() == 1);
        r.remove(action);
        assertTrue(r.isEmpty());
    }
}
