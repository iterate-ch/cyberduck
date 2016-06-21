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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionPauser;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class DefaultSessionPool extends GenericObjectPool<Session> implements SessionPool {
    private static final Logger log = Logger.getLogger(DefaultSessionPool.class);

    private static final long BORROW_MAX_WAIT_INTERVAL = 1000L;

    private final ProgressListener progress;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private final Host bookmark;

    private final int retry;

    private final Integer connections;

    /**
     * The number of times this action has been run
     */
    private ThreadLocal<Integer> repeat = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public DefaultSessionPool(final ConnectionService connect, final X509TrustManager trust, final X509KeyManager key,
                              final PathCache cache, final ProgressListener progress, final Host bookmark, final Integer connections) {
        super(new PooledSessionFactory(connect, trust, key, cache, bookmark), new GenericObjectPoolConfig() {
            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("GenericObjectPoolConfig{");
                sb.append("connections=").append(Integer.MAX_VALUE);
                sb.append('}');
                return sb.toString();
            }
        });
        this.bookmark = bookmark;
        this.retry = Integer.max(PreferencesFactory.get().getInteger("connection.retry"), connections);
        this.progress = progress;
        this.connections = connections;
    }

    @Override
    public void setConfig(final GenericObjectPoolConfig configuration) {
        configuration.setJmxEnabled(false);
        configuration.setMinIdle(0);
        configuration.setMaxTotal(connections);
        configuration.setMaxIdle(connections);
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(BORROW_MAX_WAIT_INTERVAL);
        super.setConfig(configuration);
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
                    if(this.isClosed()) {
                        throw new ConnectionCanceledException(e);
                    }
                    if(e.getCause() instanceof BackgroundException) {
                        final BackgroundException cause = (BackgroundException) e.getCause();
                        log.warn(String.format("Failure %s obtaining connection for %s", cause, this));
                        if(diagnostics.determine(cause) == FailureDiagnostics.Type.network) {
                            // Downgrade pool to single connection
                            final int max = this.getMaxTotal() - 1;
                            log.warn(String.format("Lower maximum pool size to %d connections.", max));
                            this.setMaxTotal(max);
                            this.setMaxIdle(this.getMaxIdle() - 1);
                            if(this.retry()) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Connect failed with failure %s", e));
                                }
                                // This is an automated retry. Wait some time first.
                                this.pause();
                                if(!this.isClosed()) {
                                    repeat.set(repeat.get() + 1);
                                    // Retry to connect
                                    continue;
                                }
                            }
                        }
                        throw cause;
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

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @return Greater than zero if a failed action should be repeated again
     */
    protected boolean retry() {
        // The initial connection attempt does not count
        return retry - repeat.get() > 0;
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    public void pause() {
        final int attempt = retry - repeat.get();
        final BackgroundActionPauser pauser = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return DefaultSessionPool.this.isClosed();
            }

            @Override
            public void progress(final Integer delay) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status"),
                        delay, attempt));
            }
        });
        pauser.await();
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
