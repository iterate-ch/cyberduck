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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.AutoTransferConnectionLimiter;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Throwables;

public class ConcurrentTransferWorker extends AbstractTransferWorker {
    private static final Logger log = LogManager.getLogger(ConcurrentTransferWorker.class);

    private final SessionPool source;
    private final SessionPool destination;

    private final CompletionService<TransferStatus> completion;
    // Keep number of submitted tasks
    private final AtomicInteger size = new AtomicInteger();
    private final ThreadPool pool;

    public ConcurrentTransferWorker(final SessionPool source,
                                    final SessionPool destination,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferSpeedometer meter,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final ConnectionCallback connect,
                                    final ProgressListener progressListener,
                                    final StreamListener streamListener,
                                    final NotificationService notification) {
        this(source, destination, transfer, ThreadPool.Priority.norm, options, meter, prompt, error,
                connect, progressListener, streamListener, notification);
    }

    public ConcurrentTransferWorker(final SessionPool source,
                                    final SessionPool destination,
                                    final Transfer transfer,
                                    final ThreadPool.Priority priority,
                                    final TransferOptions options,
                                    final TransferSpeedometer meter,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final ConnectionCallback connect,
                                    final ProgressListener progressListener,
                                    final StreamListener streamListener,
                                    final NotificationService notification) {
        super(transfer, options, prompt, meter, error, progressListener, streamListener, connect, notification);
        this.source = source;
        this.destination = destination;
        this.pool = ThreadPoolFactory.get(String.format("%s-transfer", new AlphanumericRandomStringService().random()),
                new AutoTransferConnectionLimiter().getLimit(transfer.getSource()), priority, new LinkedBlockingQueue<>(Integer.MAX_VALUE));
        this.completion = new ExecutorCompletionService<>(pool.executor());
    }

    @Override
    protected Session<?> borrow(final Connection type) throws BackgroundException {
        switch(type) {
            case source:
                return source.borrow(new BackgroundActionState() {
                    @Override
                    public boolean isCanceled() {
                        return ConcurrentTransferWorker.this.isCanceled();
                    }

                    @Override
                    public boolean isRunning() {
                        return true;
                    }
                });
            case destination:
                return destination.borrow(new BackgroundActionState() {
                    @Override
                    public boolean isCanceled() {
                        return ConcurrentTransferWorker.this.isCanceled();
                    }

                    @Override
                    public boolean isRunning() {
                        return true;
                    }
                });
        }
        return null;
    }

    @Override
    protected void release(final Session session, final Connection type, final BackgroundException failure) {
        switch(type) {
            case source:
                source.release(session, failure);
                break;
            case destination:
                destination.release(session, failure);
                break;
        }
    }

    @Override
    public Future<TransferStatus> submit(final TransferCallable callable) {
        log.info("Submit {} to pool", callable);
        final Future<TransferStatus> f = completion.submit(callable);
        size.incrementAndGet();
        return f;
    }

    @Override
    public void await() throws BackgroundException {
        while(size.get() > 0) {
            // Repeat until no new entries in queue found
            try {
                log.info("Await completion for {} submitted tasks in queue", size.get());
                final TransferStatus status = completion.take().get();
                log.info("Finished task with return value {}", status);
            }
            catch(InterruptedException e) {
                // Errors are handled in transfer worker error callback already
                log.warn("Unhandled failure {}", e.getMessage());
                throw new ConnectionCanceledException(e);
            }
            catch(ExecutionException e) {
                for(Throwable cause : ExceptionUtils.getThrowableList(e)) {
                    Throwables.throwIfInstanceOf(cause, BackgroundException.class);
                }
                throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
            }
            finally {
                size.decrementAndGet();
            }
        }
    }

    @Override
    protected void shutdown() {
        // Always shutdown gracefully allowing the threads to return after checking transfer status
        pool.shutdown(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentTransferWorker{");
        sb.append("source=").append(source);
        sb.append(", destination=").append(destination);
        sb.append(", pool=").append(completion);
        sb.append('}');
        return sb.toString();
    }
}
