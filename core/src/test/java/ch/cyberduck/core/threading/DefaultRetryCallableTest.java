package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultRetryCallableTest {

    @Test
    public void testCall() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final DefaultRetryCallable<Void> c = new DefaultRetryCallable<>(new Host(new TestProtocol()), 1, 0, new BackgroundExceptionCallable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                count.incrementAndGet();
                throw new ConnectionRefusedException("d", new SocketException());
            }
        }, new DisabledProgressListener(), new TransferBackgroundActionState(new TransferStatus()));
        try {
            c.call();
            fail();
        }
        catch(ConnectionRefusedException e) {
            // Expected
        }
        assertEquals(2, count.get());
    }
}
