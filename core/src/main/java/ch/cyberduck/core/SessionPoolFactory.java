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
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;

public class SessionPoolFactory {

    private SessionPoolFactory() {
        //
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark) {
        return create(controller, cache, bookmark,
                LoginCallbackFactory.get(controller),
                HostKeyCallbackFactory.get(controller, bookmark.getProtocol()),
                PasswordStoreFactory.get()
        );
    }

    public static SessionPool create(final Controller controller, final PathCache cache, final Host bookmark,
                                     final LoginCallback login, final HostKeyCallback key, final HostPasswordStore keychain) {
        return new DefaultSessionPool(
                new LoginConnectionService(
                        login,
                        key,
                        keychain,
                        controller,
                        controller),
                new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)),
                new KeychainX509KeyManager(bookmark), cache, controller, bookmark
        )
                .withMaxIdle(PreferencesFactory.get().getInteger("connection.pool.maxidle"))
                .withMaxTotal(PreferencesFactory.get().getInteger("connection.pool.maxtotal"));
    }
}
