package ch.cyberduck.core.pool;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.commons.lang3.StringUtils;

public interface SessionPool {
    SessionPool DISCONNECTED = new DisconnectedSessionPool();

    /**
     * Borrow session from pool
     *
     * @param callback Cancel callback
     * @return Open connection
     * @throws BackgroundException Failure if opening connection fails
     */
    Session<?> borrow(BackgroundActionState callback) throws BackgroundException;

    /**
     * Release session to pool for reuse
     *
     * @param session Connection
     * @param failure Failure or null
     */
    void release(Session<?> session, BackgroundException failure);

    /**
     * Close all idle connections in pool
     */
    void evict();

    /**
     * @return Connection configuration
     */
    Host getHost();

    /**
     * @return Shared vaults for sessions
     */
    VaultRegistry getVaultRegistry();

    /**
     * @return Current pool connection state
     */
    Session.State getState();

    /**
     * Obtain feature from connection type
     */
    <T> T getFeature(final Class<T> type);

    /**
     * Shutdown connection pool
     */
    void shutdown();

    interface Callback {
        boolean isCanceled();
    }

    final class DisconnectedSessionPool implements SessionPool {
        private static final Host DISCONNECTED = new Host(new DisabledProtocol());

        @Override
        public Session<?> borrow(final BackgroundActionState callback) {
            return null;
        }

        @Override
        public void release(final Session<?> session, final BackgroundException failure) {
            //
        }

        @Override
        public void evict() {
            //
        }

        @Override
        public Host getHost() {
            return DISCONNECTED;
        }

        @Override
        public VaultRegistry getVaultRegistry() {
            return VaultRegistry.DISABLED;
        }

        @Override
        public Session.State getState() {
            return Session.State.closed;
        }

        @Override
        public <T> T getFeature(final Class<T> type) {
            return null;
        }

        @Override
        public void shutdown() {
            //
        }

        private static class DisabledProtocol extends AbstractProtocol {
            @Override
            public String getName() {
                return StringUtils.EMPTY;
            }

            @Override
            public String getIdentifier() {
                return StringUtils.EMPTY;
            }

            @Override
            public String getDescription() {
                return StringUtils.EMPTY;
            }

            @Override
            public Scheme getScheme() {
                return Scheme.file;
            }
        }
    }

    final class SingleSessionPool implements SessionPool {
        private final Session<?> session;

        public SingleSessionPool(final Session<?> session) {
            this.session = session;
        }

        public SingleSessionPool(final Session<?> session, final VaultRegistry registry) {
            this.session = session;
        }

        @Override
        public Session<?> borrow(final BackgroundActionState callback) {
            return session;
        }

        @Override
        public void release(final Session<?> session, final BackgroundException failure) {
        }

        @Override
        public void evict() {
        }

        @Override
        public Host getHost() {
            return session.getHost();
        }

        @Override
        public VaultRegistry getVaultRegistry() {
            return VaultRegistry.DISABLED;
        }

        @Override
        public Session.State getState() {
            return Session.State.open;
        }

        @Override
        public <T> T getFeature(final Class<T> type) {
            return session.getFeature(type);
        }

        @Override
        public void shutdown() {
        }
    }
}
