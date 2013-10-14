package ch.cyberduck.ui.action;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
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

    private Transfer transfer;

    private ObjectPool<Session> pool;

    private CompletionService<TransferStatus> completion;

    private BlockingQueue<Future<Path>> queue;

    private AtomicInteger size = new AtomicInteger();

    private ProgressListener progressListener;

    private TranscriptListener transcriptListener;

    public ConcurrentTransferWorker(final ConnectionService connect,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferPrompt prompt, final TransferErrorCallback error,
                                    final ProgressListener progressListener,
                                    final TranscriptListener transcriptListener) {
        this(connect, transfer, options, prompt, error, progressListener, transcriptListener,
                Preferences.instance().getInteger("queue.session.pool.size"));
    }

    public ConcurrentTransferWorker(final ConnectionService connect,
                                    final Transfer transfer, final TransferOptions options,
                                    final TransferPrompt prompt, final TransferErrorCallback error,
                                    final ProgressListener progressListener,
                                    final TranscriptListener transcriptListener,
                                    final Integer connections) {
        super(transfer, options, prompt, error);
        this.connect = connect;
        this.progressListener = progressListener;
        this.transcriptListener = transcriptListener;
        final GenericObjectPool.Config configuration = new GenericObjectPool.Config();
        configuration.maxActive = connections;
        configuration.maxIdle = connections;
        configuration.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        pool = new GenericObjectPoolFactory(new SessionPool(transfer.getHost()), configuration).createPool();
        queue = new LinkedBlockingQueue<Future<Path>>();
        completion = new ExecutorCompletionService(Executors.newFixedThreadPool(connections, new NamedThreadFactory("transfer")), queue);
    }

    @Override
    protected Session<?> borrow() throws BackgroundException {
        try {
            final Session session = pool.borrowObject();
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            return session;
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            throw new BackgroundException(e.getMessage(), e);
        }
    }

    @Override
    protected void release(final Session session) throws BackgroundException {
        try {
            pool.returnObject(session);
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            throw new BackgroundException(e.getMessage(), e);
        }
    }

    @Override
    protected void submit(final TransferCallable callable) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s", callable));
        }
        completion.submit(callable);
        size.incrementAndGet();
    }

    @Override
    protected void complete() throws BackgroundException {
        // Await termination for submitted tasks in queue
        for(int i = 0; i < size.get(); i++) {
            try {
                final TransferStatus status = completion.take().get();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Finished %s", status));
                }
            }
            catch(InterruptedException e) {
                throw new BackgroundException(e.getMessage(), e);
            }
            catch(ExecutionException e) {
                final Throwable cause = e.getCause();
                if(cause instanceof BackgroundException) {
                    throw (BackgroundException) cause;
                }
                throw new BackgroundException(cause);
            }
        }
        size.set(0);
    }

    @Override
    public void cleanup(final Boolean result) {
        try {
            pool.close();
        }
        catch(Exception e) {
            log.warn(String.format("Failure closing connection pool %s", e.getMessage()));
        }
    }

    private final class SessionPool extends BasePoolableObjectFactory<Session> {

        private Host host;

        private SessionPool(final Host host) {
            this.host = host;
        }

        @Override
        public Session makeObject() {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create new session for host %s in pool", host));
            }
            final Session session = SessionFactory.createSession(host);
            session.addProgressListener(progressListener);
            session.addTranscriptListener(transcriptListener);
            return session;
        }

        @Override
        public boolean validateObject(final Session session) {
            return true;
        }

        @Override
        public void activateObject(final Session session) throws BackgroundException {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Activate session %s", session));
            }
            connect.check(session, Cache.empty());
        }

        @Override
        public void destroyObject(final Session session) throws BackgroundException {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Destroy session %s", session));
            }
            session.removeProgressListener(progressListener);
            session.removeTranscriptListener(transcriptListener);
            session.close();
        }
    }
}