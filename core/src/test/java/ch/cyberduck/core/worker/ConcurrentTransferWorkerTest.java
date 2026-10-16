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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
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
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
                ConnectionCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
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
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
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
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
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
                                 final TransferOptions options, final TransferStatus segment,
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
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
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
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
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
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
        );
        int workers = 1000;
        final CountDownLatch entry = new CountDownLatch(workers);
        for(int i = 0; i < workers; i++) {
            worker.submit(new TransferWorker.TransferCallable() {
                @Override
                public TransferStatus call() {
                    entry.countDown();
                    return new TransferStatus();
                }
            });
        }
        worker.await();
        assertEquals(0, entry.getCount());
        worker.cleanup(true);
    }

    @Test
    public void testShutdownPoolOnFailure() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final Transfer transfer = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.file)),
                new NullLocal("l")) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) {
                return TransferAction.overwrite;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return new OverwriteFilter(new UploadSymlinkResolver(null, Collections.emptyList()), source) {
                    @Override
                    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
                        throw new AccessDeniedException(file.getAbsolute());
                    }
                };
            }
        };
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        new DefaultVaultRegistry(new DisabledPasswordCallback()),
                        new DisabledTranscriptListener(), host), SessionPool.DISCONNECTED, transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
        );
        final Set<Thread> baseline = transferThreads();
        BackgroundException failure = null;
        try {
            worker.run(worker.borrow(ConcurrentTransferWorker.Connection.source));
            fail();
        }
        catch(AccessDeniedException e) {
            // Failure in thread of transfer pool
            failure = e;
        }
        worker.cleanup(false, failure);
        assertEquals(baseline, awaitTransferThreads(baseline));
    }

    @Test
    public void testShutdownPoolOnTransferFailure() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final Set<String> transfers = ConcurrentHashMap.newKeySet();
        final Transfer transfer = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.file)),
                new NullLocal("l")) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) {
                return TransferAction.overwrite;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return new OverwriteFilter(new UploadSymlinkResolver(null, Collections.emptyList()), source) {
                    @Override
                    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        return true;
                    }

                    @Override
                    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        return new TransferStatus();
                    }

                    @Override
                    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
                        //
                    }
                };
            }

            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local, final TransferOptions options,
                                 final TransferStatus segment, final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) throws BackgroundException {
                transfers.add(Thread.currentThread().getName());
                throw new AccessDeniedException(file.getAbsolute());
            }
        };
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        new DefaultVaultRegistry(new DisabledPasswordCallback()),
                        new DisabledTranscriptListener(), host), SessionPool.DISCONNECTED, transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
        );
        final Set<Thread> baseline = transferThreads();
        BackgroundException failure = null;
        try {
            worker.run(worker.borrow(ConcurrentTransferWorker.Connection.source));
            fail();
        }
        catch(AccessDeniedException e) {
            // Failure in thread of transfer pool after prepare
            failure = e;
        }
        assertEquals(1, transfers.size());
        assertTrue(transfers.iterator().next().matches("[A-Za-z0-9]+-transfer-\\d+"));
        worker.cleanup(false, failure);
        assertEquals(baseline, awaitTransferThreads(baseline));
    }

    @Test
    public void testRetryAfterTransferFailure() throws Exception {
        final Host host = new Host(new TestProtocol(), "localhost", new Credentials("u", "p"));
        final AtomicInteger attempts = new AtomicInteger();
        final Transfer transfer = new UploadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.file)),
                new NullLocal("l")) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt, final ListProgressListener listener) {
                return TransferAction.overwrite;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
                return new OverwriteFilter(new UploadSymlinkResolver(null, Collections.emptyList()), source) {
                    @Override
                    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        return true;
                    }

                    @Override
                    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) {
                        return new TransferStatus();
                    }

                    @Override
                    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
                        //
                    }

                    @Override
                    public void complete(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) {
                        //
                    }
                };
            }

            @Override
            public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local, final TransferOptions options,
                                 final TransferStatus segment, final ConnectionCallback prompt,
                                 final ProgressListener progress, final StreamListener listener) throws BackgroundException {
                if(attempts.incrementAndGet() == 1) {
                    throw new AccessDeniedException(file.getAbsolute());
                }
                segment.setComplete();
            }
        };
        final LoginConnectionService connection = new TestLoginConnectionService();
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                new DefaultSessionPool(connection, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        new DefaultVaultRegistry(new DisabledPasswordCallback()),
                        new DisabledTranscriptListener(), host), SessionPool.DISCONNECTED, transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                LoginCallback.noop, ProgressListener.noop, StreamListener.noop, new DisabledNotificationService()
        );
        final Set<Thread> baseline = transferThreads();
        try {
            worker.run(worker.borrow(ConcurrentTransferWorker.Connection.source));
            fail();
        }
        catch(AccessDeniedException e) {
            // Retry with same worker as in background action
        }
        assertTrue(worker.run(worker.borrow(ConcurrentTransferWorker.Connection.source)));
        assertEquals(2, attempts.get());
        worker.cleanup(true, null);
        assertEquals(baseline, awaitTransferThreads(baseline));
    }

    /**
     * @return Threads of transfer pools
     */
    private static Set<Thread> transferThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().matches("[A-Za-z0-9]+-transfer-\\d+")).collect(Collectors.toSet());
    }

    private static Set<Thread> awaitTransferThreads(final Set<Thread> expected) throws InterruptedException {
        Set<Thread> threads = transferThreads();
        for(int i = 0; i < 50 && !threads.equals(expected); i++) {
            Thread.sleep(100L);
            threads = transferThreads();
        }
        return threads;
    }
}
