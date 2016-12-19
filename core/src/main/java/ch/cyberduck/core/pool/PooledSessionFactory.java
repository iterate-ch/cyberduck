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

import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.vault.LoadingVaultLookupListener;
import ch.cyberduck.core.vault.VaultLookupListener;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;

public class PooledSessionFactory extends BasePooledObjectFactory<Session> {
    private static final Logger log = Logger.getLogger(PooledSessionFactory.class);

    private final ConnectionService connect;
    private final X509TrustManager trust;
    private final X509KeyManager key;
    private final PasswordCallback password;
    private final VaultLookupListener listener;
    private final PathCache cache;
    private final Host bookmark;

    public PooledSessionFactory(final ConnectionService connect, final X509TrustManager trust, final X509KeyManager key,
                                final PasswordCallback password, final VaultLookupListener listener, final PathCache cache, final Host bookmark) {
        this.connect = connect;
        this.trust = trust;
        this.key = key;
        this.password = password;
        this.listener = listener;
        this.cache = cache;
        this.bookmark = bookmark;
    }

    @Override
    public Session create() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new session for host %s in pool", bookmark));
        }
        final Session<?> session = SessionFactory.create(bookmark, trust, key);
        session.addListener(new SessionPoolVaultListener(session, new LoadingVaultLookupListener(listener, session, password)));
        return session;
    }

    @Override
    public PooledObject<Session> wrap(Session session) {
        return new DefaultPooledObject<Session>(session);
    }

    @Override
    public void activateObject(final PooledObject<Session> p) throws BackgroundException {
        final Session session = p.getObject();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Activate session %s", session));
        }
        connect.check(session, cache);
    }

    @Override
    public void passivateObject(final PooledObject<Session> p) throws Exception {
        final Session session = p.getObject();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Pause session %s", session));
        }
    }

    @Override
    public void destroyObject(final PooledObject<Session> p) throws BackgroundException {
        final Session session = p.getObject();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Destroy session %s", session));
        }
        session.close();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionPool{");
        sb.append("host=").append(bookmark);
        sb.append('}');
        return sb.toString();
    }


    private static final class SessionPoolVaultListener implements VaultLookupListener {
        private static final Logger log = Logger.getLogger(SessionPoolVaultListener.class);

        private final Session<?> session;
        private final VaultLookupListener proxy;

        public SessionPoolVaultListener(final Session<?> session, final VaultLookupListener proxy) {
            this.session = session;
            this.proxy = proxy;
        }

        @Override
        public void found(final Vault vault) throws BackgroundException {
            if(log.isInfoEnabled()) {
                log.info(String.format("Pooling vault %s", vault));
            }
            session.withVault(vault);
            proxy.found(new PooledVault(vault));
        }
    }
}
