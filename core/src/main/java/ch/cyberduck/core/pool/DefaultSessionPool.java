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
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.EvictionConfig;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

public class DefaultSessionPool implements SessionPool {
    private static final Logger log = Logger.getLogger(DefaultSessionPool.class);

    private static final long BORROW_MAX_WAIT_INTERVAL = 1000L;
    private static final int POOL_WARNING_THRESHOLD = 5;

    private final FailureDiagnostics<BackgroundException> diagnostics
            = new DefaultFailureDiagnostics();

    private final ConnectionService connect;
    private final TranscriptListener transcript;
    private final PathCache cache;
    private final Host bookmark;

    private final VaultRegistry registry;

    private final GenericObjectPool<Session> pool;

    private SessionPool features = SessionPool.DISCONNECTED;

    public DefaultSessionPool(final ConnectionService connect, final X509TrustManager trust, final X509KeyManager key,
                              final VaultRegistry registry, final PathCache cache,
                              final TranscriptListener transcript,
                              final Host bookmark) {
        this.connect = connect;
        this.registry = registry;
        this.cache = cache;
        this.bookmark = bookmark;
        this.transcript = transcript;
        final GenericObjectPoolConfig configuration = new GenericObjectPoolConfig();
        configuration.setJmxEnabled(false);
        configuration.setEvictionPolicyClassName(CustomPoolEvictionPolicy.class.getName());
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(BORROW_MAX_WAIT_INTERVAL);
        this.pool = new GenericObjectPool<Session>(new PooledSessionFactory(connect, trust, key, cache, bookmark, registry), configuration);
        final AbandonedConfig abandon = new AbandonedConfig();
        abandon.setUseUsageTracking(true);
        this.pool.setAbandonedConfig(abandon);
    }

    public DefaultSessionPool(final ConnectionService connect, final VaultRegistry registry, final PathCache cache,
                              final TranscriptListener transcript, final Host bookmark, final GenericObjectPool<Session> pool) {
        this.connect = connect;
        this.transcript = transcript;
        this.cache = cache;
        this.bookmark = bookmark;
        this.registry = registry;
        this.pool = pool;
    }

    public static final class CustomPoolEvictionPolicy implements EvictionPolicy<Session<?>> {
        public CustomPoolEvictionPolicy() {
            //
        }

        @Override
        public boolean evict(final EvictionConfig config, final PooledObject<Session<?>> underTest, final int idleCount) {
            log.warn(String.format("Evict idle session %s from pool", underTest));
            return true;
        }
    }

    public DefaultSessionPool withMinIdle(final int count) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure with min idle %d", count));
        }
        pool.setMinIdle(count);
        return this;
    }

    public DefaultSessionPool withMaxIdle(final int count) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure with max idle %d", count));
        }
        pool.setMaxIdle(count);
        return this;
    }

    public DefaultSessionPool withMaxTotal(final int count) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure with max total %d", count));
        }
        pool.setMaxTotal(count);
        return this;
    }

    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        final Integer numActive = pool.getNumActive();
        if(numActive > POOL_WARNING_THRESHOLD) {
            log.warn(String.format("Possibly large number of open connections (%d) in pool %s", numActive, this));
        }
        try {
            /*
             * The number of times this action has been run
             */
            int repeat = 0;
            while(!callback.isCanceled()) {
                try {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Borrow session from pool %s", this));
                    }
                    final Session<?> session = pool.borrowObject();
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Borrowed session %s from pool %s", session, this));
                    }
                    if(DISCONNECTED == features) {
                        features = new StatelessSessionPool(connect, session, cache, transcript, registry);
                    }
                    return session.withListener(transcript);
                }
                catch(IllegalStateException e) {
                    throw new ConnectionCanceledException(e);
                }
                catch(NoSuchElementException e) {
                    if(pool.isClosed()) {
                        throw new ConnectionCanceledException(e);
                    }
                    final Throwable cause = e.getCause();
                    if(null == cause) {
                        log.warn(String.format("Timeout borrowing session from pool %s. Wait for another %dms", this, BORROW_MAX_WAIT_INTERVAL));
                        // Timeout
                        continue;
                    }
                    if(cause instanceof BackgroundException) {
                        final BackgroundException failure = (BackgroundException) cause;
                        log.warn(String.format("Failure %s obtaining connection for %s", failure, this));
                        if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                            final int max = Math.max(1, pool.getMaxIdle() - 1);
                            log.warn(String.format("Lower maximum idle pool size to %d connections.", max));
                            pool.setMaxIdle(max);
                            // Clear pool from idle connections
                            pool.clear();
                        }
                        throw failure;
                    }
                    log.error(String.format("Borrowing session from pool %s failed with %s", this, e));
                    throw new DefaultExceptionMappingService().map(cause);
                }
            }
            throw new ConnectionCanceledException();
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
    public void release(final Session<?> session, final BackgroundException failure) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Release session %s to pool", session));
        }
        try {
            if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                try {
                    session.interrupt();
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Failure interrupting session %s prior releasing to pool. %s", session, e.getDetail()));
                }
                finally {
                    try {
                        // Activation of this method decrements the active count and attempts to destroy the instance
                        pool.invalidateObject(session.removeListener(transcript));
                    }
                    catch(Exception e) {
                        log.warn(String.format("Failure invalidating session %s in pool. %s", session, e.getMessage()));
                    }
                }
            }
            else {
                pool.returnObject(session);
            }
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failed to release session %s. %s", session, e.getMessage()));
        }
    }

    @Override
    public void evict() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Clear idle connections in pool %s", this));
        }
        pool.clear();
    }

    @Override
    public void shutdown() {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Close connection pool %s", this));
            }
            this.evict();
            pool.close();
        }
        catch(Exception e) {
            log.warn(String.format("Failure closing connection pool %s", e.getMessage()));
        }
        finally {
            registry.clear();
        }
    }

    @Override
    public Host getHost() {
        return bookmark;
    }

    @Override
    public PathCache getCache() {
        return cache;
    }

    @Override
    public VaultRegistry getVault() {
        return registry;
    }

    public int getNumActive() {
        return pool.getNumActive();
    }

    public int getNumIdle() {
        return pool.getNumIdle();
    }

    @Override
    public Session.State getState() {
        if(pool.isClosed()) {
            return Session.State.closed;
        }
        if(cache.isEmpty()) {
            return Session.State.opening;
        }
        if(0 == pool.getNumIdle()) {
            return Session.State.opening;
        }
        return Session.State.open;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(DISCONNECTED == features) {
            return SessionFactory.create(bookmark, new DisabledX509TrustManager(), new DefaultX509KeyManager()).getFeature(type);
        }
        return features.getFeature(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultSessionPool{");
        sb.append("bookmark=").append(bookmark);
        sb.append('}');
        return sb.toString();
    }
}
