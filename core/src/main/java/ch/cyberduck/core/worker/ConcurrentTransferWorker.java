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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTransferWorker extends AbstractTransferWorker {
    private static final Logger log = Logger.getLogger(ConcurrentTransferWorker.class);

    private final SessionPool source;
    private final SessionPool destination;

    private final CompletionService<TransferStatus> completion;
    // Keep number of submited tasks
    private final AtomicInteger size = new AtomicInteger();

    public ConcurrentTransferWorker(final SessionPool source,
                                    final SessionPool destination,
                                    final Transfer transfer,
                                    final TransferOptions options,
                                    final TransferSpeedometer meter,
                                    final TransferPrompt prompt,
                                    final TransferErrorCallback error,
                                    final ConnectionCallback connectionCallback,
                                    final ProgressListener progressListener,
                                    final StreamListener streamListener) {
        super(transfer, options, prompt, meter, error, progressListener, streamListener, connectionCallback);
        this.source = source;
        this.destination = destination;
        final ThreadPool pool = ThreadPoolFactory.get("transfer",
                transfer.getSource().getTransferType() == Host.TransferType.newconnection ?
                        1 : PreferencesFactory.get().getInteger("queue.maxtransfers"));
        this.completion = new ExecutorCompletionService<TransferStatus>(pool.executor());
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
    protected void release(final Session session, final Connection type) {
        switch(type) {
            case source:
                source.release(session, null);
                break;
            case destination:
                destination.release(session, null);
                break;
        }
    }

    @Override
    public Future<TransferStatus> submit(final TransferCallable callable) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s to pool", callable));
        }
        final Future<TransferStatus> f = completion.submit(callable);
        size.incrementAndGet();
        return f;
    }

    @Override
    public void await() throws BackgroundException {
        while(size.get() > 0) {
            // Repeat until no new entries in queue found
            if(log.isInfoEnabled()) {
                log.info(String.format("Await completion for %d submitted tasks in queue", size.get()));
            }
            try {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Await completion for %d submitted tasks in queue", size.get()));
                }
                final TransferStatus status = completion.take().get();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Finished task with return value %s", status));
                }
            }
            catch(InterruptedException | ExecutionException e) {
                // Errors are handled in transfer worker error callback already
                log.warn(String.format("Unhandled failure %s", e));
                throw new BackgroundException(e);
            }
            finally {
                size.decrementAndGet();
            }
        }
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