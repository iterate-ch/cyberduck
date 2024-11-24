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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
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
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
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
        final DefaultSessionPool pool = new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()),
                new DisabledTranscriptListener(), host);
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool, SessionPool.DISCONNECTED, t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledConnectionCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
    }

    @Test
    public void testBorrow() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final DefaultSessionPool pool = new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()),
                new DisabledTranscriptListener(), host);
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool, SessionPool.DISCONNECTED, t, new TransferOptions(), new TransferSpeedometer(t),
                new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        assertNotSame(worker.borrow(ConcurrentTransferWorker.Connection.source), worker.borrow(ConcurrentTransferWorker.Connection.source));
        worker.cleanup(true);
    }

    @Test
    public void testSessionReuse() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final DefaultSessionPool pool = new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()),
                new DisabledTranscriptListener(), host);
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool.withMaxTotal(1), SessionPool.DISCONNECTED, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        // Override default transfer queue size
        pool.withMaxTotal(1);
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
        assertEquals(Session.State.closed, session.getState());
        final Session<?> reuse = worker.borrow(ConcurrentTransferWorker.Connection.source);
        assertSame(session, reuse);
        final CyclicBarrier lock = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertSame(session, worker.borrow(ConcurrentTransferWorker.Connection.source));
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
        worker.release(reuse, ConcurrentTransferWorker.Connection.source, null);
        lock.await(1, TimeUnit.MINUTES);
        worker.cleanup(true);
    }

    @Test
    public void testConcurrentSessions() throws Exception {
        final int files = 20;
        final int connections = 3;
        final Set<Path> transferred = ConcurrentHashMap.newKeySet();
        final List<TransferItem> list = new ArrayList<>();
        for(int i = 1; i <= files; i++) {
            list.add(new TransferItem(new Path("/t" + i, EnumSet.of(Path.Type.file)), new NullLocal("/t" + i)));
        }
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final Transfer t = new DownloadTransfer(host, list) {

            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local,
                                 final TransferOptions options, final TransferStatus overall, final TransferStatus segment,
                                 final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) {
                assertNotNull(source);
                transferred.add(file);
            }

            @Override
            public AbstractDownloadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return new AbstractDownloadFilter(new DisabledDownloadSymlinkResolver(), source, null) {
                    @Override
                    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        assertFalse(transferred.contains(file));
                        return true;
                    }

                    @Override
                    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        assertFalse(transferred.contains(file));
                        return new TransferStatus();
                    }

                    @Override
                    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
                        assertFalse(transferred.contains(file));
                    }

                    @Override
                    public void complete(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
                        assertTrue(transferred.contains(file));
                    }
                };
            }
        };
        final LoginConnectionService connection = new TestLoginConnectionService();
        final DefaultSessionPool pool = new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()),
                new DisabledTranscriptListener(), host);
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool, SessionPool.DISCONNECTED, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        pool.withMaxTotal(connections);
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        assertTrue(worker.run(session));
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
        for(int i = 1; i <= files; i++) {
            assertTrue(transferred.contains(new Path("/t" + i, EnumSet.of(Path.Type.file))));
        }
        worker.cleanup(true);
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
                        new DefaultVaultRegistry(new DisabledPasswordCallback()),
                        new DisabledTranscriptListener(), host), SessionPool.DISCONNECTED, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        assertNotNull(session);
        final CyclicBarrier lock = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.await(1, TimeUnit.MINUTES);
                }
                catch(InterruptedException | BrokenBarrierException | TimeoutException e) {
                    fail();
                }
            }
        }).start();
        Thread.sleep(2000L);
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
    }

    @Test
    public void testAwait() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final Transfer transfer = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        new DefaultVaultRegistry(new DisabledPasswordCallback()),
                        new DisabledTranscriptListener(), host), SessionPool.DISCONNECTED, transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        int workers = 1000;
        final CountDownLatch entry = new CountDownLatch(workers);
        for(int i = 0; i < workers; i++) {
            worker.submit(new TransferWorker.TransferCallable() {
                @Override
                public TransferStatus call() {
                    entry.countDown();
                    return new TransferStatus().complete();
                }
            });
        }
        worker.await();
        assertEquals(0, entry.getCount());
        worker.cleanup(true);
    }
}
