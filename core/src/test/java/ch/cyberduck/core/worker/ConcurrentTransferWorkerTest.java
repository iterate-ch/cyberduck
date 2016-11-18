/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

package ch.cyberduck.core.worker;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class ConcurrentTransferWorkerTest {

    @Test
    public void testDoubleRelease() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, 1), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );
        final Session<?> session = worker.borrow();
        worker.release(session);
        worker.release(session);
    }

    @Test
    public void testBorrow() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, 2), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );
        assertNotSame(worker.borrow(), worker.borrow());
    }

    @Test
    public void testSessionReuse() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, 1), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );
        final Session<?> session = worker.borrow();
        worker.release(session);
        assertEquals(Session.State.closed, session.getState());
        final Session<?> reuse = worker.borrow();
        assertSame(session, reuse);
        final CyclicBarrier lock = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertSame(session, worker.borrow());
                    try {
                        lock.await(1, TimeUnit.MINUTES);
                    }
                    catch(InterruptedException | BrokenBarrierException | TimeoutException e) {
                        fail();
                    }
                }
                catch(BackgroundException e) {
                    fail();
                }
            }
        }).start();
        worker.release(reuse);
        lock.await(1, TimeUnit.MINUTES);
    }

    @Test
    public void testConcurrentSessions() throws Exception {
        final int files = 5;
        final int connections = 3;
        final CountDownLatch lock = new CountDownLatch(files);
        final CountDownLatch d = new CountDownLatch(connections - 1);
        final Set<Path> transferred = new HashSet<Path>();
        final List<TransferItem> list = new ArrayList<TransferItem>();
        for(int i = 1; i <= files; i++) {
            list.add(new TransferItem(new Path("/t" + i, EnumSet.of(Path.Type.file)), new NullLocal("/t" + i)));
        }
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new DownloadTransfer(host, list
        ) {

            @Override
            public void transfer(final Session<?> session, final Path file, final Local local,
                                 final TransferOptions options, final TransferStatus status,
                                 final ConnectionCallback callback,
                                 final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
                assertNotNull(session);
                transferred.add(file);
                lock.countDown();
                d.countDown();
                try {
                    d.await();
                }
                catch(InterruptedException e) {
                    fail();
                }
            }

            @Override
            public AbstractDownloadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
                return new AbstractDownloadFilter(null, session, null) {
                    @Override
                    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
                        assertFalse(transferred.contains(file));
                        return true;
                    }

                    @Override
                    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
                        assertFalse(transferred.contains(file));
                        return new TransferStatus();
                    }

                    @Override
                    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
                        assertFalse(transferred.contains(file));
                    }

                    @Override
                    public void complete(final Path file, final Local local, final TransferOptions options, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
                        assertTrue(transferred.contains(file));
                    }
                };
            }
        };
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, connections), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );

        assertTrue(worker.run(null));
        lock.await(1, TimeUnit.MINUTES);
        for(int i = 1; i <= files; i++) {
            assertTrue(transferred.contains(new Path("/t" + i, EnumSet.of(Path.Type.file))));
        }
    }

    @Test
    public void testBorrowTimeoutNoSessionAvailable() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, 1), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );
        final Session<?> session = worker.borrow();
        assertNotNull(session);
        final CyclicBarrier lock = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertSame(session, worker.borrow());
                    try {
                        lock.await(1, TimeUnit.MINUTES);
                    }
                    catch(InterruptedException | BrokenBarrierException | TimeoutException e) {
                        fail();
                    }
                }
                catch(BackgroundException e) {
                    fail();
                }
            }
        }).start();
        Thread.sleep(2000L);
        worker.release(session);
    }

    @Test
    public void testAwait() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        PathCache.empty(), new DisabledProgressListener(), host, 1), t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener()
        );
        int workers = 1000;
        final CountDownLatch entry = new CountDownLatch(workers);
        for(int i = 0; i < workers; i++) {
            worker.submit(new TransferWorker.TransferCallable() {
                @Override
                public TransferStatus call() throws BackgroundException {
                    entry.countDown();
                    return new TransferStatus().complete();
                }
            });
        }
        worker.await();
        assertTrue(entry.getCount() == 0);

    }

}