package ch.cyberduck.core;

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

import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.pool.StatefulSessionPool;
import ch.cyberduck.core.pool.StatelessSessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultRegistryFactory;

import org.apache.log4j.Logger;

public class SessionPoolFactory {
    private static final Logger log = Logger.getLogger(SessionPoolFactory.class);

    private static final Preferences preferences = PreferencesFactory.get();

    private SessionPoolFactory() {
        //
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark) {
        final HostPasswordStore keychain = PasswordStoreFactory.get();
        final LoginConnectionService connect = new LoginConnectionService(LoginCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol()), keychain, controller);
        return create(connect, controller, cache, bookmark,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                new KeychainX509KeyManager(bookmark),
                VaultRegistryFactory.create(keychain, PasswordCallbackFactory.get(controller)));
    }

    public static SessionPool create(final ConnectionService connect, final TranscriptListener transcript,
                                     final PathCache cache, final Host bookmark,
                                     final X509TrustManager x509TrustManager, final X509KeyManager x509KeyManager,
                                     final VaultRegistry vault) {
        switch(bookmark.getProtocol().getType()) {
            case s3:
            case googlestorage:
            case dropbox:
            case googledrive:
            case swift:
            case dav:
            case azure:
            case b2:
                // Statless protocol
                return stateless(connect, transcript, cache, bookmark, x509TrustManager, x509KeyManager, vault);
            case ftp:
            case irods:
                // Stateful
            default:
                if(log.isInfoEnabled()) {
                    log.info(String.format("Create new pooled connection pool for %s", bookmark));
                }
                return new DefaultSessionPool(connect, x509TrustManager, x509KeyManager, vault, cache, transcript, bookmark)
                        .withMinIdle(preferences.getInteger("connection.pool.minidle"))
                        .withMaxIdle(preferences.getInteger("connection.pool.maxidle"))
                        .withMaxTotal(preferences.getInteger("connection.pool.maxtotal"));
        }
    }

    /**
     * @return Single stateless session
     */
    protected static SessionPool stateless(final ConnectionService connect, final TranscriptListener transcript,
                                           final PathCache cache, final Host bookmark,
                                           final X509TrustManager trust, final X509KeyManager key,
                                           final VaultRegistry vault) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Create new stateless connection pool for %s", bookmark));
        }
        final Session<?> session = SessionFactory.create(bookmark, trust, key);
        return new StatelessSessionPool(connect, session, cache, transcript, vault);
    }

    /**
     * @return Single stateful session
     */
    protected static SessionPool stateful(final ConnectionService connect, final TranscriptListener transcript,
                                          final PathCache cache, final Host bookmark,
                                          final X509TrustManager trust, final X509KeyManager key,
                                          final VaultRegistry vault) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Create new stateful connection pool for %s", bookmark));
        }
        final Session<?> session = SessionFactory.create(bookmark, trust, key);
        return new StatefulSessionPool(connect, session, cache, transcript, vault);
    }
}
