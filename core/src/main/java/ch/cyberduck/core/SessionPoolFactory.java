package ch.cyberduck.core;

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

import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.pool.StatefulSessionPool;
import ch.cyberduck.core.pool.StatelessSessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.vault.VaultRegistryFactory;

import org.apache.log4j.Logger;

public class SessionPoolFactory {
    private static final Logger log = Logger.getLogger(SessionPoolFactory.class);

    private SessionPoolFactory() {
        //
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark) {
        return create(controller, cache, bookmark,
                PasswordStoreFactory.get(),
                LoginCallbackFactory.get(controller),
                PasswordCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol())
        );
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark,
                                     final HostPasswordStore keychain, final LoginCallback login,
                                     final PasswordCallback password, final HostKeyCallback key) {
        switch(bookmark.getProtocol().getType()) {
            case ftp:
            case irods:
                // Stateful
                return stateful(controller, cache, bookmark, keychain, login, password, key);
            default:
                return pooled(controller, cache, bookmark, keychain, login, password, key);
        }
    }

    public static SessionPool pooled(final Controller controller, final PathCache cache, final Host bookmark) {
        return pooled(controller, cache, bookmark,
                PasswordStoreFactory.get(),
                LoginCallbackFactory.get(controller),
                PasswordCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol())
        );
    }

    public static SessionPool pooled(final Controller controller, final PathCache cache, final Host bookmark,
                                     final HostPasswordStore keychain, final LoginCallback login,
                                     final PasswordCallback password, final HostKeyCallback key) {
        switch(bookmark.getProtocol().getType()) {
            case sftp:
                // Statless
                return stateless(controller, cache, bookmark, keychain, login, password, key);
            case s3:
            case googlestorage:
            case dropbox:
            case googledrive:
            case swift:
            case dav:
            case azure:
            case b2:
                // HTTP connection pool
                return stateless(controller, cache, bookmark, keychain, login, password, key);
            default:
                if(log.isInfoEnabled()) {
                    log.info(String.format("Create new pooled connection pool for %s", bookmark));
                }
                return new DefaultSessionPool(
                        new LoginConnectionService(
                                login,
                                key,
                                keychain,
                                controller,
                                controller),
                        new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                        new KeychainX509KeyManager(bookmark), VaultRegistryFactory.create(keychain, password), cache, controller, bookmark
                )
                        .withMinIdle(PreferencesFactory.get().getInteger("connection.pool.minidle"))
                        .withMaxIdle(PreferencesFactory.get().getInteger("connection.pool.maxidle"))
                        .withMaxTotal(PreferencesFactory.get().getInteger("connection.pool.maxtotal"));
        }
    }

    public static SessionPool stateless(final Controller controller, final PathCache cache, final Host bookmark) {
        return stateless(controller, cache, bookmark,
                PasswordStoreFactory.get(),
                LoginCallbackFactory.get(controller),
                PasswordCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol())
        );
    }

    public static SessionPool stateless(final Controller controller, final PathCache cache, final Host bookmark,
                                        final HostPasswordStore keychain, final LoginCallback login,
                                        final PasswordCallback password, final HostKeyCallback key) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Create new stateless connection pool for %s", bookmark));
        }
        final Session<?> session = SessionFactory.create(bookmark,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                new KeychainX509KeyManager(bookmark));
        return new StatelessSessionPool(
                new LoginConnectionService(
                        login,
                        key,
                        keychain,
                        controller,
                        controller),
                session, cache, VaultRegistryFactory.create(keychain, password)
        );
    }

    public static SessionPool stateful(final Controller controller, final PathCache cache, final Host bookmark) {
        return stateful(controller, cache, bookmark,
                PasswordStoreFactory.get(),
                LoginCallbackFactory.get(controller),
                PasswordCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol())
        );
    }

    public static SessionPool stateful(final Controller controller, final PathCache cache, final Host bookmark,
                                       final HostPasswordStore keychain, final LoginCallback login,
                                       final PasswordCallback password, final HostKeyCallback key) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Create new stateful connection pool for %s", bookmark));
        }
        final Session<?> session = SessionFactory.create(bookmark,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                new KeychainX509KeyManager(bookmark));
        return new StatefulSessionPool(
                new LoginConnectionService(
                        login,
                        key,
                        keychain,
                        controller,
                        controller),
                session, cache, VaultRegistryFactory.create(keychain, password)
        );
    }
}
