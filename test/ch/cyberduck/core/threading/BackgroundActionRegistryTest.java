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

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class BackgroundActionRegistryTest extends AbstractTestCase {

    @Test
    public void testGetCurrent() throws Exception {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final CountDownLatch lock = new CountDownLatch(1);
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public void run() throws BackgroundException {
            }

            @Override
            public void finish() {
                super.finish();
                lock.countDown();
            }
        };
        assertTrue(r.add(action));
        action.finish();
        lock.await();
        assertFalse(r.contains(action));
        assertNull(r.getCurrent());
    }

    @Test
    public void testCancel() throws Exception {
        BackgroundActionRegistry r = new BackgroundActionRegistry();
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public void run() throws BackgroundException {
            }
        };
        assertTrue(r.add(action));
        action.cancel();
        assertFalse(r.contains(action));
        assertNull(r.getCurrent());
    }
}
