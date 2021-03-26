package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class AutoreleaseStreamListenerTest {

    @Test
    public void testAutorelease() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        new Thread(new Runnable() {
            final AutoreleaseStreamListener listener = new AutoreleaseStreamListener(
                ThreadLocal.withInitial(() -> new AutoreleaseActionOperationBatcher(10)));

            @Override
            public void run() {
                listener.recv(1L);
                lock.countDown();
            }
        }).start();
        lock.await();
    }
}
