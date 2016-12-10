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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.cryptomator.CryptoInvalidFilesizeException;
import ch.cyberduck.core.cryptomator.LookupVault;
import ch.cyberduck.core.cryptomator.VaultLookupListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionPauser;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.EvictionConfig;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class DefaultSessionPool implements SessionPool {
    private static final Logger log = Logger.getLogger(DefaultSessionPool.class);

    private static final long BORROW_MAX_WAIT_INTERVAL = 1000L;
    private static final int POOL_WARNING_THRESHOLD = 5;

    private final Preferences preferences
            = PreferencesFactory.get();

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private final ConnectionService connect;
    private final ProgressListener progress;
    private final PasswordStore keychain;
    private final PasswordCallback password;
    private final PathCache cache;
    private final Host bookmark;

    private final GenericObjectPool<Session> pool;

    private SessionPool features = SessionPool.DISCONNECTED;

    /**
     * Vault finder
     */
    private final LookupVault finder;
    private Vault vault = Vault.DISABLED;

    private int retry = preferences.getInteger("connection.retry");

    public DefaultSessionPool(final ConnectionService connect, final X509TrustManager trust, final X509KeyManager key,
                              final PasswordStore keychain, final PasswordCallback password,
                              final PathCache cache, final ProgressListener progress, final Host bookmark) {
        this.connect = connect;
        this.keychain = keychain;
        this.password = password;
        this.cache = cache;
        this.bookmark = bookmark;
        this.progress = progress;
        final GenericObjectPoolConfig configuration = new GenericObjectPoolConfig();
        configuration.setJmxEnabled(false);
        configuration.setEvictionPolicyClassName(CustomPoolEvictionPolicy.class.getName());
        configuration.setBlockWhenExhausted(true);
        configuration.setMaxWaitMillis(BORROW_MAX_WAIT_INTERVAL);
        this.pool = new GenericObjectPool<Session>(
                new PooledSessionFactory(connect, trust, key, keychain, password, cache, bookmark), configuration);
        final AbandonedConfig abandon = new AbandonedConfig();
        abandon.setUseUsageTracking(true);
        this.pool.setAbandonedConfig(abandon);
        this.finder = new LookupVault(keychain, password, new SessionPoolVaultListener());
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

    public DefaultSessionPool withRetry(final int retry) {
        this.retry = retry;
        return this;
    }

    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        final Integer numActive = pool.getNumActive();
        if(numActive > POOL_WARNING_THRESHOLD) {
            log.warn(String.format("Possibly large number of open connections (%d) in pool %s", numActive, pool));
        }
        try {
            /**
             * The number of times this action has been run
             */
            int repeat = 0;
            while(!callback.isCanceled()) {
                try {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Borrow session from pool %s", pool));
                    }
                    final Session<?> session = pool.borrowObject();
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Borrowed session %s from pool %s", session, pool));
                    }
                    if(DISCONNECTED == features) {
                        features = new SingleSessionPool(connect, session, cache, keychain, password);
                    }
                    if(PreferencesFactory.get().getBoolean("cryptomator.enable")) {
                        session.withVault(Vault.DISABLED == vault ? finder : new PooledVault(vault));
                    }
                    return session;
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
                        // fix null pointer
                        final BackgroundException failure = (BackgroundException) cause;
                        log.warn(String.format("Failure %s obtaining connection for %s", failure, this));
                        if(this.retry(failure, retry - repeat++)) {
                            continue;
                        }
                        throw failure;
                    }
                    log.error(String.format("Borrowing session from pool %s failed with %s", this, e));
                    throw new BackgroundException(e);
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

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @param failure   Connect failure
     * @param remaining Remaining number of connect attempts. The initial connection attempt does not count
     * @return Greater than zero if a failed action should be repeated again
     */
    protected boolean retry(final BackgroundException failure, final int remaining) {
        if(remaining > 0) {
            if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Retry for failure %s", failure));
                }
                final int max = Math.max(1, pool.getMaxIdle() - 1);
                log.warn(String.format("Lower maximum idle pool size to %d connections.", max));
                pool.setMaxIdle(max);
                // Clear pool from idle connections
                pool.clear();
                // This is an automated retry. Wait some time first.
                this.pause(remaining);
                // Retry to connect
                return true;
            }
        }
        return false;
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    protected void pause(final int attempt) {
        final BackgroundActionPauser pauser = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return pool.isClosed();
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
    public void release(final Session<?> session, final BackgroundException failure) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Release session %s to pool", session));
        }
        try {
            pool.returnObject(session);
            if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                try {
                    pool.invalidateObject(session);
                }
                catch(Exception e) {
                    log.warn(String.format("Failure invalidating session %s in pool", session));
                }
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
            vault.close();
            pool.close();
        }
        catch(Exception e) {
            log.warn(String.format("Failure closing connection pool %s", e.getMessage()));
        }
    }

    @Override
    public Host getHost() {
        return bookmark;
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

    private class SessionPoolVaultListener implements VaultLookupListener {
        @Override
        public void found(final Vault found) {
            vault = found;
        }
    }

    private class PooledVault implements Vault {
        final Vault delegate;

        public PooledVault(final Vault delegate) {
            this.delegate = vault;
        }

        @Override
        public Vault create(final Session<?> session, final String region) throws BackgroundException {
            return delegate.create(session, region);
        }

        @Override
        public Vault load(final Session<?> session) throws BackgroundException {
            return delegate.load(session);
        }

        @Override
        public void close() {
            log.warn(String.format("Keep vault %s open for session pool", delegate));
        }

        @Override
        public boolean contains(final Path file) {
            return delegate.contains(file);
        }

        @Override
        public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
            return delegate.encrypt(session, file);
        }

        @Override
        public Path encrypt(final Session<?> session, final Path file, final boolean metadata) throws BackgroundException {
            return delegate.encrypt(session, file, metadata);
        }

        @Override
        public Path decrypt(final Session<?> session, final Path directory, final Path file) throws BackgroundException {
            return delegate.decrypt(session, directory, file);
        }

        @Override
        public long toCiphertextSize(final long cleartextFileSize) {
            return delegate.toCiphertextSize(cleartextFileSize);
        }

        @Override
        public long toCleartextSize(final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
            return delegate.toCleartextSize(ciphertextFileSize);
        }

        @Override
        public <T> T getFeature(final Session<?> session, final Class<T> type, final T impl) {
            return delegate.getFeature(session, type, impl);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PooledVault{");
            sb.append("delegate=").append(delegate);
            sb.append('}');
            return sb.toString();
        }
    }
}
