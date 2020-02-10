package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HistoryCollectionTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.get().register(new TestProtocol());
    }

    @Test
    public void testAdd() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final CountDownLatch loaded = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        final HistoryCollection c = new HistoryCollection(new Local("src/test/resources/history")) {
            @Override
            public void load() throws AccessDeniedException {
                super.load();
                loaded.countDown();
                try {
                    lock.await(1, TimeUnit.SECONDS);
                }
                catch(InterruptedException e) {
                    fail();
                }
                exit.countDown();
            }
        };
        new Thread(() -> {
            try {
                c.load();
            }
            catch(AccessDeniedException e) {
                //
            }
        }).start();
        loaded.await(1, TimeUnit.SECONDS);
        assertEquals(1, c.size());
        final Host host = c.get(0);
        // Add again to history upon connect before history finished loading
        assertFalse(c.add(host));
        lock.countDown();
        exit.await(1, TimeUnit.SECONDS);
        assertEquals(1, c.size());
    }
}
