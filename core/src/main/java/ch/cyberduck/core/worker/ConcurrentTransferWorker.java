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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

public class ConcurrentTransferWorker extends AbstractTransferWorker {
    private static final Logger log = Logger.getLogger(ConcurrentTransferWorker.class);

    private final SessionPool pool;

    private final ThreadPool<TransferStatus> completion;

    public ConcurrentTransferWorker(final ConnectionService connect,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferSpeedometer meter, final TransferPrompt prompt, final TransferErrorCallback error,
                                    final TransferItemCallback transferItemCallback, final ConnectionCallback connectionCallback,
                                    final ProgressListener progressListener, final StreamListener streamListener,
                                    final X509TrustManager trust, final X509KeyManager key, final PathCache cache,
                                    final Integer connections) {
        super(transfer, options, prompt, meter, error, transferItemCallback, progressListener, streamListener, connectionCallback);
        pool = new DefaultSessionPool(connect, trust, key, cache, progressListener, transfer.getHost(), connections);
        completion = new DefaultThreadPool<TransferStatus>(connections, "transfer");
    }

    @Override
    protected Session<?> borrow() throws BackgroundException {
        try {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            Session session = pool.borrow();
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
            pool.release(session);
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
        completion.execute(callable);
    }

    @Override
    public void await() throws BackgroundException {
        completion.await();
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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentTransferWorker{");
        sb.append("completion=").append(completion);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}