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

package ch.cyberduck.core.pool;

import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundActionStateCancelCallback;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatelessSessionPool implements SessionPool {
    private static final Logger log = LogManager.getLogger(StatelessSessionPool.class);

    private final ConnectionService connect;
    private final TranscriptListener transcript;
    private final Session<?> session;
    private final VaultRegistry registry;

    private final Lock sync = new ReentrantLock();

    public StatelessSessionPool(final ConnectionService connect, final Session<?> session,
                                final TranscriptListener transcript, final VaultRegistry registry) {
        this.connect = connect;
        this.transcript = transcript;
        this.session = session.withRegistry(registry);
        this.registry = registry;
    }


    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        sync.lock();
        try {
            connect.check(session.withListener(transcript), new BackgroundActionStateCancelCallback(callback));
            return session;
        }
        finally {
            sync.unlock();
        }
    }

    @Override
    public void release(final Session<?> conn, final BackgroundException failure) {
        if(failure != null) {
            log.warn("Keep connection {} alive with failure {}", conn, failure);
        }
    }

    @Override
    public void evict() {
        sync.lock();
        try {
            try {
                session.close();
            }
            catch(BackgroundException e) {
                log.warn("Ignore failure {} closing connection", e.getMessage());
            }
            finally {
                session.removeListener(transcript);
                registry.clear();
            }
        }
        finally {
            sync.unlock();
        }
    }

    @Override
    public void shutdown() {
        sync.lock();
        try {
            try {
                session.close();
            }
            catch(BackgroundException e) {
                log.warn("Failure {} closing session", e.getMessage());
            }
            finally {
                registry.clear();
            }
        }
        finally {
            sync.unlock();
        }
    }

    @Override
    public Session.State getState() {
        return session.getState();
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        return session.getFeature(type);
    }

    @Override
    public Host getHost() {
        return session.getHost();
    }

    @Override
    public VaultRegistry getVaultRegistry() {
        return registry;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatelessSessionPool{");
        sb.append("session=").append(session);
        sb.append('}');
        return sb.toString();
    }
}
