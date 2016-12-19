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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.vault.LoadingVaultLookupListener;
import ch.cyberduck.core.vault.VaultLookupListener;

import org.apache.log4j.Logger;

public class SingleSessionPool implements SessionPool, VaultLookupListener {
    private static final Logger log = Logger.getLogger(SingleSessionPool.class);

    private final ConnectionService connect;
    private final Session<?> session;
    private final PathCache cache;

    public SingleSessionPool(final ConnectionService connect, final Session<?> session, final PathCache cache,
                             final PasswordCallback password) {
        this.connect = connect;
        this.session = session;
        this.session.addListener(new LoadingVaultLookupListener(this, session, password));
        this.cache = cache;
    }


    @Override
    public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
        connect.check(session, cache);
        return session;
    }

    @Override
    public void release(final Session<?> session, final BackgroundException failure) {
        //
    }

    @Override
    public void evict() {
        try {
            session.close();
        }
        catch(BackgroundException e) {
            log.warn(String.format("Ignore failure closing connection. %s", e.getMessage()));
        }
    }

    @Override
    public void shutdown() {
        try {
            session.close();
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure closing session. %s", e.getMessage()));
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("SingleSessionPool{");
        sb.append("session=").append(session);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void found(final Vault vault) throws BackgroundException {
        session.withVault(vault);
    }
}
