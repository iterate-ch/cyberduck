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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

public class DefaultSessionPool extends GenericObjectPool<Session> implements SessionPool {
    private static final Logger log = Logger.getLogger(DefaultSessionPool.class);

    private static final long BORROW_MAX_WAIT_INTERVAL = 1000L;
    private static final GenericObjectPoolConfig configuration = new GenericObjectPoolConfig() {

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GenericObjectPoolConfig{");
            sb.append("connections=").append(Integer.MAX_VALUE);
            sb.append('}');
            return sb.toString();
        }
    };

    static {
        configuration.setJmxEnabled(false);
        configuration.setMinIdle(0);
        configuration.setMaxTotal(Integer.MAX_VALUE);
        configuration.setMaxIdle(Integer.MAX_VALUE);
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(BORROW_MAX_WAIT_INTERVAL);
    }

    private final Host bookmark;

    public DefaultSessionPool(final ConnectionService connect, final X509TrustManager trust, final X509KeyManager key,
                              final PathCache cache, final Host bookmark) {
        super(new PooledSessionFactory(connect, trust, key, cache, bookmark), configuration);
        this.bookmark = bookmark;
    }

    @Override
    public Session<?> borrow() throws BackgroundException {
        try {
            Session session;
            while(true) {
                try {
                    session = this.borrowObject();
                    break;
                }
                catch(NoSuchElementException e) {
                    if(e.getCause() instanceof BackgroundException) {
                        throw (BackgroundException) e.getCause();
                    }
                    if(null == e.getCause()) {
                        log.warn(String.format("Timeout borrowing session from pool %s. Wait for another %dms", this, BORROW_MAX_WAIT_INTERVAL));
                        // Timeout
                        continue;
                    }
                    log.error(String.format("Borrowing session from pool %s failed with %s", this, e));
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
    public void release(final Session session) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Release session %s to pool", session));
        }
        try {
            this.returnObject(session);
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failed to release session %s. %s", session, e.getMessage()));
        }
    }

    @Override
    public void close() {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Close connection pool %s", this));
            }
            super.close();
        }
        catch(Exception e) {
            log.warn(String.format("Failure closing connection pool %s", e.getMessage()));
        }
    }

    @Override
    public Host getHost() {
        return bookmark;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultSessionPool{");
        sb.append("bookmark=").append(bookmark);
        sb.append('}');
        return sb.toString();
    }
}
