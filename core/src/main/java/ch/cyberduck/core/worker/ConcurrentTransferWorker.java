package ch.cyberduck.core.worker;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionPauser;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTransferWorker extends AbstractTransferWorker {
    private static final Logger log = Logger.getLogger(ConcurrentTransferWorker.class);

    private static final long BORROW_MAX_WAIT_INTERVAL = 1000L;

    private final GenericObjectPool<Session> pool;

    private final CompletionService<TransferStatus> completion;

    private final AtomicInteger size = new AtomicInteger();

    private final ProgressListener progress;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private final int retry;

    /**
     * The number of times this action has been run
     */
    private ThreadLocal<Integer> repeat = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public ConcurrentTransferWorker(final ConnectionService connect,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferSpeedometer meter, final TransferPrompt prompt, final TransferErrorCallback error,
                                    final TransferItemCallback transferItemCallback, final ConnectionCallback connectionCallback,
                                    final ProgressListener progressListener, final StreamListener streamListener,
                                    final X509TrustManager trust, final X509KeyManager key, final PathCache cache,
                                    final Integer connections) {
        super(transfer, options, prompt, meter, error, transferItemCallback, progressListener, streamListener, connectionCallback);
        final GenericObjectPoolConfig configuration = new GenericObjectPoolConfig() {
            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("GenericObjectPoolConfig{");
                sb.append("connections=").append(connections);
                sb.append('}');
                return sb.toString();
            }
        };
        configuration.setJmxEnabled(false);
        configuration.setMinIdle(0);
        configuration.setMaxTotal(connections);
        configuration.setMaxIdle(connections);
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(BORROW_MAX_WAIT_INTERVAL);
        progress = progressListener;
        retry = Integer.max(PreferencesFactory.get().getInteger("connection.retry"), connections);
        pool = new GenericObjectPool<Session>(
                new SessionPool(connect, trust, key, cache, transfer.getHost()), configuration) {
            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("GenericObjectPool{");
                sb.append("configuration=").append(configuration);
                sb.append('}');
                return sb.toString();
            }
        };
        completion = new ExecutorCompletionService<TransferStatus>(
                Executors.newFixedThreadPool(connections, new NamedThreadFactory("transfer")),
                new LinkedBlockingQueue<Future<TransferStatus>>());
    }

    @Override
    protected Session<?> borrow() throws BackgroundException {
        try {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            Session session;
            while(true) {
                try {
                    session = pool.borrowObject();
                    break;
                }
                catch(NoSuchElementException e) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException(e);
                    }
                    if(e.getCause() instanceof BackgroundException) {
                        final BackgroundException cause = (BackgroundException) e.getCause();
                        log.warn(String.format("Failure %s obtaining connection for %s", cause, this));
                        if(diagnostics.determine(cause) == FailureDiagnostics.Type.network) {
                            // Downgrade pool to single connection
                            final int max = pool.getMaxTotal() - 1;
                            log.warn("Lower maximum pool size to " + max + " connections.");
                            pool.setMaxTotal(max);
                            pool.setMaxIdle(pool.getMaxIdle() - 1);
                            if(this.retry()) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Connect failed with failure %s", e));
                                }
                                // This is an automated retry. Wait some time first.
                                this.pause();
                                if(!isCanceled()) {
                                    repeat.set(repeat.get() + 1);
                                    // Retry to connect
                                    continue;
                                }
                            }
                        }
                        throw cause;
                    }
                    if(null == e.getCause()) {
                        log.warn(String.format("Timeout borrowing session from pool %s. Wait for another %dms", pool, BORROW_MAX_WAIT_INTERVAL));
                        // Timeout
                        continue;
                    }
                    log.error(String.format("Borrowing session from pool %s failed with %s", pool, e));
                    throw new BackgroundException(e);
                }
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Borrow session %s from pool", session));
            }
            return session;
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            if(e.getCause() instanceof BackgroundException) {
                throw ((BackgroundException) e.getCause());
            }
            throw new BackgroundException(e.getMessage(), e);
        }
    }

    @Override
    protected void release(final Session session) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Release session %s to pool", session));
        }
        try {
            pool.returnObject(session);
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failed to release session %s. %s", session, e.getMessage()));
        }
    }

    @Override
    public void submit(final TransferCallable callable) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s to pool", callable));
        }
        completion.submit(callable);
        size.incrementAndGet();
    }

    @Override
    public void await() throws BackgroundException {
        while(size.get() > 0) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Await termination for %d submitted tasks in queue", size.get()));
            }
            try {
                final TransferStatus status = completion.take().get();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Finished %s", status));
                }
            }
            catch(InterruptedException e) {
                throw new ConnectionCanceledException(e);
            }
            catch(ExecutionException e) {
                final Throwable cause = e.getCause();
                if(cause instanceof BackgroundException) {
                    throw (BackgroundException) cause;
                }
                throw new BackgroundException(cause);
            }
            finally {
                size.decrementAndGet();
            }
        }
    }

    @Override
    public void cleanup(final Boolean result) {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Close connection pool %s", pool));
            }
            pool.close();
        }
        catch(Exception e) {
            log.warn(String.format("Failure closing connection pool %s", e.getMessage()));
        }
        super.cleanup(result);
    }


    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @return Greater than zero if a failed action should be repeated again
     */
    protected boolean retry() {
        // The initial connection attempt does not count
        return retry - repeat.get() > 0;
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    public void pause() {
        final int attempt = retry - repeat.get();
        final BackgroundActionPauser pauser = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return ConcurrentTransferWorker.this.isCanceled();
            }

            @Override
            public void progress(final Integer delay) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status"),
                        delay, attempt));
            }
        });
        pauser.await(progress);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentTransferWorker{");
        sb.append("size=").append(size);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}