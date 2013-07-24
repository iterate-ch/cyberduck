package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.download.DownloadTransfer;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class QueueTest extends AbstractTestCase {

    @Test
    public void testAddRemove() throws Exception {
        final Queue queue = new Queue();
        final DownloadTransfer transfer = new DownloadTransfer(new NullSession(new Host("t")), new Path("/t", Path.DIRECTORY_TYPE));
        queue.add(transfer, new DisabledProgressListener());
        queue.remove(transfer);
    }

    @Test
    public void testConcurrent() throws Exception {
        final Queue queue = new Queue(1);
        final DownloadTransfer transfer = new DownloadTransfer(new NullSession(new Host("t")), new Path("/t", Path.DIRECTORY_TYPE));
        queue.add(transfer, new DisabledProgressListener());
        final AtomicBoolean added = new AtomicBoolean();
        final CyclicBarrier wait = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(new DownloadTransfer(new NullSession(new Host("t")), new Path("/t", Path.DIRECTORY_TYPE)), new DisabledProgressListener());
                added.set(true);
                try {
                    wait.await();
                }
                catch(InterruptedException e) {
                    fail();
                }
                catch(BrokenBarrierException e) {
                    fail();
                }
            }
        }).start();
        assertFalse(added.get());
        queue.remove(transfer);
        wait.await();
        assertTrue(added.get());
    }
}
