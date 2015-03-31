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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItemCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Id$
 */
public class ConcurrentTransferWorker extends AbstractTransferWorker {
    private static final Logger log = Logger.getLogger(ConcurrentTransferWorker.class);

    private ConnectionService connect;

    private GenericObjectPool<Session> pool;

    private CompletionService<TransferStatus> completion;

    private AtomicInteger size = new AtomicInteger();

    private X509TrustManager trust;

    private X509KeyManager key;

    public ConcurrentTransferWorker(final ConnectionService connect,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferSpeedometer meter, final TransferPrompt prompt, final TransferErrorCallback error,
                                    final TransferItemCallback transferItemCallback, final ConnectionCallback connectionCallback,
                                    final ProgressListener progressListener, final StreamListener streamListener,
                                    final X509TrustManager trust, final X509KeyManager key,
                                    final Integer connections) {
        super(transfer, options, prompt, meter, error, transferItemCallback, progressListener, streamListener, connectionCallback);
        this.connect = connect;
        final GenericObjectPoolConfig configuration = new GenericObjectPoolConfig();
        configuration.setJmxEnabled(false);
        configuration.setMaxTotal(connections);
        configuration.setMaxIdle(connections);
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(1000L);
        pool = new GenericObjectPool<Session>(
                new SessionPool(transfer.getHost()), configuration);
        completion = new ExecutorCompletionService<TransferStatus>(
                Executors.newFixedThreadPool(connections, new NamedThreadFactory("transfer")),
                new LinkedBlockingQueue<Future<TransferStatus>>());
        this.trust = trust;
        this.key = key;
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
                        throw new ConnectionCanceledException();
                    }
                    if(e.getCause() instanceof BackgroundException) {
                        throw (BackgroundException) e.getCause();
                    }
                    if(null == e.getCause()) {
                        log.warn(String.format("Timeout borrowing session from pool %s", pool));
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
        pool.returnObject(session);
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
        // Await termination for submitted tasks in queue
        final int queued = size.get();
        for(int i = 0; i < queued; i++) {
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

    private final class SessionPool extends BasePooledObjectFactory<Session> {

        private Host host;

        private SessionPool(final Host host) {
            this.host = host;
        }

        @Override
        public Session create() {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create new session for host %s in pool", host));
            }
            return SessionFactory.create(host, trust, key);
        }

        @Override
        public PooledObject<Session> wrap(Session session) {
            return new DefaultPooledObject<Session>(session);
        }

        @Override
        public void activateObject(final PooledObject<Session> p) throws BackgroundException {
            final Session session = p.getObject();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Activate session %s", session));
            }
            connect.check(session, PathCache.empty());
        }

        @Override
        public void destroyObject(final PooledObject<Session> p) throws BackgroundException {
            final Session session = p.getObject();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Destroy session %s", session));
            }
            session.close();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SessionPool{");
            sb.append("host=").append(host);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentTransferWorker{");
        sb.append("size=").append(size);
        sb.append('}');
        return sb.toString();
    }
}