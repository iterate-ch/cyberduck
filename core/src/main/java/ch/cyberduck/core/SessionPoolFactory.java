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
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.vault.VaultRegistryFactory;

public class SessionPoolFactory {

    private SessionPoolFactory() {
        //
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark) {
        return PreferencesFactory.get().getBoolean("connection.pool.enable") ?
                pooled(controller, cache, bookmark) : single(controller, cache, bookmark);
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
                .withRetry(PreferencesFactory.get().getInteger("connection.retry"))
                .withMinIdle(PreferencesFactory.get().getInteger("connection.pool.minidle"))
                .withMaxIdle(PreferencesFactory.get().getInteger("connection.pool.maxidle"))
                .withMaxTotal(PreferencesFactory.get().getInteger("connection.pool.maxtotal"));
    }

    public static SessionPool single(final Controller controller, final PathCache cache, final Host bookmark) {
        return single(controller, cache, bookmark,
                PasswordStoreFactory.get(),
                LoginCallbackFactory.get(controller),
                PasswordCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol())
        );
    }

    public static SessionPool single(final Controller controller, final PathCache cache, final Host bookmark,
                                     final HostPasswordStore keychain, final LoginCallback login,
                                     final PasswordCallback password, final HostKeyCallback key) {
        final Session<?> session = SessionFactory.create(bookmark,
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                new KeychainX509KeyManager(bookmark));
        return new SingleSessionPool(
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
