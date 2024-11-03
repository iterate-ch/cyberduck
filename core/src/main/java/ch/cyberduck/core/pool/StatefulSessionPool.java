package ch.cyberduck.core.pool;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatefulSessionPool extends StatelessSessionPool {
    private static final Logger log = LogManager.getLogger(StatefulSessionPool.class);

    private final FailureDiagnostics<BackgroundException> diagnostics = new DefaultFailureDiagnostics();
    private final Lock lock = new ReentrantLock();
    private final ConnectionService connect;
    private final Session<?> session;

    public StatefulSessionPool(final ConnectionService connect, final Session<?> session,
                               final TranscriptListener transcript, final VaultRegistry registry) {
        super(connect, session, transcript, registry);
        this.connect = connect;
        this.session = session;
    }

    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        try {
            if(log.isDebugEnabled()) {
                log.debug("Acquire lock for connection {}", session);
            }
            lock.lock();
        }
        catch(IllegalMonitorStateException e) {
            log.warn("Failure acquiring lock for {}", session);
            throw new ConnectionCanceledException(e);
        }
        return super.borrow(callback);
    }

    @Override
    public void release(final Session<?> conn, final BackgroundException failure) {
        try {
            if(failure != null) {
                if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                    if(log.isWarnEnabled()) {
                        log.warn("Close session {} after failure {}", session, failure);
                    }
                    try {
                        connect.close(conn);
                    }
                    catch(final BackgroundException e) {
                        log.warn("Ignore failure {} closing connection", e);
                    }
                }
            }
        }
        finally {
            try {
                if(log.isDebugEnabled()) {
                    log.debug("Release lock for connection {}", session);
                }
                lock.unlock();
            }
            catch(IllegalMonitorStateException ignored) {
                log.warn("Failure releasing lock for {}", session);
            }
        }
    }

    @Override
    public void evict() {
        super.evict();
        try {
            lock.unlock();
        }
        catch(IllegalMonitorStateException ignored) {
            log.warn("Failure releasing lock for {}", session);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            lock.unlock();
        }
        catch(IllegalMonitorStateException ignored) {
            log.warn("Failure releasing lock for {}", session);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatefulSessionPool{");
        sb.append("lock=").append(lock);
        sb.append(", session=").append(session);
        sb.append('}');
        return sb.toString();
    }
}
