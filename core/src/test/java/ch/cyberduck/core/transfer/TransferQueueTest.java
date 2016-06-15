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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class TransferQueueTest {

    @Test
    public void testAddRemove() throws Exception {
        final TransferQueue queue = new TransferQueue(1);
        final DownloadTransfer d1 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t1", EnumSet.of(Path.Type.directory)), null);
        final DownloadTransfer d2 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t2", EnumSet.of(Path.Type.directory)), null);
        queue.add(d1, new DisabledProgressListener());
        final CountDownLatch c = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(d2, new DisabledProgressListener());
                c.countDown();
            }
        }).start();
        assertTrue(c.getCount() == 1);
        queue.remove(d1);
        assertTrue(c.await(1, TimeUnit.SECONDS));
        assertTrue(c.getCount() == 0);
    }

    @Test
    public void testConcurrent() throws Exception {
        final TransferQueue queue = new TransferQueue(1);
        final DownloadTransfer transfer = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t", EnumSet.of(Path.Type.directory)), null);
        queue.add(transfer, new DisabledProgressListener());
        final AtomicBoolean added = new AtomicBoolean();
        final CyclicBarrier wait = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(new DownloadTransfer(new Host(new TestProtocol()), new Path("/t", EnumSet.of(Path.Type.directory)), null), new DisabledProgressListener());
                added.set(true);
                try {
                    wait.await();
                }
                catch(InterruptedException | BrokenBarrierException e) {
                    fail();
                }
            }
        }).start();
        assertFalse(added.get());
        queue.remove(transfer);
        wait.await();
        assertTrue(added.get());
    }

    @Test
    public void testResize() throws Exception {
        final TransferQueue queue = new TransferQueue(1);
        final DownloadTransfer d1 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t1", EnumSet.of(Path.Type.directory)), null);
        final DownloadTransfer d2 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t2", EnumSet.of(Path.Type.directory)), null);
        queue.add(d1, new DisabledProgressListener());
        final CountDownLatch c = new CountDownLatch(1);
        final AtomicBoolean set = new AtomicBoolean();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(d2, new DisabledProgressListener() {
                    @Override
                    public void message(final String message) {
                        assertEquals("Maximum allowed connections exceeded. Waiting", message);
                        set.set(true);
                    }
                });
                c.countDown();
            }
        }).start();
        assertTrue(c.getCount() == 1);
        assertFalse(c.await(1, TimeUnit.SECONDS));
        queue.resize(2);
        assertTrue(c.await(1, TimeUnit.SECONDS));
        assertTrue(set.get());
    }

    @Test
    public void testResizeMultipleWaiting() throws Exception {
        final TransferQueue queue = new TransferQueue(1);
        final DownloadTransfer d1 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t1", EnumSet.of(Path.Type.directory)), null);
        final DownloadTransfer d2 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t2", EnumSet.of(Path.Type.directory)), null);
        final DownloadTransfer d3 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t3", EnumSet.of(Path.Type.directory)), null);
        queue.add(d1, new DisabledProgressListener());
        final CountDownLatch c = new CountDownLatch(1);
        final AtomicBoolean set1 = new AtomicBoolean();
        final AtomicBoolean set2 = new AtomicBoolean();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(d2, new DisabledProgressListener() {
                    @Override
                    public void message(final String message) {
                        assertEquals("Maximum allowed connections exceeded. Waiting", message);
                        set1.set(true);
                    }
                });
                c.countDown();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(d3, new DisabledProgressListener() {
                    @Override
                    public void message(final String message) {
                        assertEquals("Maximum allowed connections exceeded. Waiting", message);
                        set2.set(true);
                    }
                });
            }
        }).start();
        assertTrue(c.getCount() == 1);
        assertFalse(c.await(1, TimeUnit.SECONDS));
        assertTrue(set1.get());
        assertTrue(set2.get());
        queue.resize(2);
        assertTrue(c.await(1, TimeUnit.SECONDS));
        assertTrue(c.getCount() == 0);
    }

    @Test
    public void testRemoveWhileWaiting() throws Exception {
        final TransferQueue queue = new TransferQueue(1);
        final DownloadTransfer d1 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t1", EnumSet.of(Path.Type.directory)), null);
        final DownloadTransfer d2 = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t2", EnumSet.of(Path.Type.directory)), null);
        queue.add(d1, new DisabledProgressListener());
        final CountDownLatch c = new CountDownLatch(1);
        final AtomicBoolean set = new AtomicBoolean();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(d2, new DisabledProgressListener() {
                    @Override
                    public void message(final String message) {
                        assertEquals("Maximum allowed connections exceeded. Waiting", message);
                        set.set(true);
                    }
                });
                c.countDown();
            }
        }).start();
        assertFalse(c.await(1, TimeUnit.SECONDS));
        assertTrue(c.getCount() == 1);
        assertTrue(set.get());
        queue.remove(d2);
        assertTrue(c.await(1, TimeUnit.SECONDS));
        assertTrue(c.getCount() == 0);
    }
}
