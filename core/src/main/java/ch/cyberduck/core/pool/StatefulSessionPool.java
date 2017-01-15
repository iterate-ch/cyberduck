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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatefulSessionPool extends StatelessSessionPool {
    private static final Logger log = Logger.getLogger(StatefulSessionPool.class);

    private final Lock lock = new ReentrantLock();
    private final Session<?> session;

    public StatefulSessionPool(final ConnectionService connect, final Session<?> session,
                               final PathCache cache, final VaultRegistry registry) {
        super(connect, session, cache, registry);
        this.session = session;
    }

    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Acquire lock for connection %s", session));
            }
            lock.lock();
        }
        catch(IllegalMonitorStateException e) {
            log.warn(String.format("Failure acquiring lock for %s", session));
            throw new ConnectionCanceledException(e);
        }
        return super.borrow(callback);
    }

    @Override
    public void release(final Session<?> conn, final BackgroundException failure) {
        super.release(conn, failure);
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Release lock for connection %s", session));
            }
            lock.unlock();
        }
        catch(IllegalMonitorStateException ignored) {
            log.warn(String.format("Failure releasing lock for %s", session));
        }
    }

    @Override
    public void evict() {
        super.evict();
        try {
            lock.unlock();
        }
        catch(IllegalMonitorStateException ignored) {
            log.warn(String.format("Failure releasing lock for %s", session));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            lock.unlock();
        }
        catch(IllegalMonitorStateException ignored) {
            log.warn(String.format("Failure releasing lock for %s", session));
        }
    }
}
