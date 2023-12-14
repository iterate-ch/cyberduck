package ch.cyberduck.core.transfer;

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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;

import org.junit.Test;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SegmentRetryCallableTest {

    @Test
    public void testRetry() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final SegmentRetryCallable<Void> c = new SegmentRetryCallable<Void>(new Host(new TestProtocol(Scheme.file)),
            2, 0,
            new BackgroundExceptionCallable<Void>() {
                @Override
                public Void call() throws BackgroundException {
                    throw new ConnectionRefusedException("d", new SocketException());
                }
            }, new TransferStatus(), new BytecountStreamListener()) {
            @Override
            public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
                count.incrementAndGet();
                return super.retry(failure, progress, cancel);
            }
        };
        try {
            c.call();
            fail();
        }
        catch(ConnectionRefusedException e) {
            // Expected
        }
        assertEquals(3, count.get());
    }
}
