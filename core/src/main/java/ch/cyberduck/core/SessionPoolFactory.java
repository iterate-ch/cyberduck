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
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultRegistryFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class SessionPoolFactory {
    private static final Logger log = LogManager.getLogger(SessionPoolFactory.class);

    private SessionPoolFactory() {
        //
    }

    public static SessionPool create(final Controller controller, final Host bookmark) {
        return create(controller, bookmark, controller);
    }

    public static SessionPool create(final Controller controller, final Host bookmark,
                                     final ProgressListener listener) {
        return create(controller, bookmark, listener, Usage.transfer);
    }

    public static SessionPool create(final Controller controller, final Host bookmark,
                                     final Usage... usage) {
        return create(controller, bookmark, controller, usage);
    }

    public static SessionPool create(final Controller controller, final Host bookmark,
                                     final ProgressListener listener, final TranscriptListener transcript, final Usage... usage) {
        return create(controller, bookmark, PasswordStoreFactory.get(), LoginCallbackFactory.get(controller), HostKeyCallbackFactory.get(controller,
            bookmark.getProtocol()), listener, transcript, usage);
    }

    public static SessionPool create(final Controller controller, final Host bookmark,
                                     final ProgressListener listener, final Usage... usage) {
        return create(controller, bookmark, PasswordStoreFactory.get(), LoginCallbackFactory.get(controller), HostKeyCallbackFactory.get(controller,
            bookmark.getProtocol()), listener, controller, usage);
    }

    public static SessionPool create(final Controller controller, final Host bookmark,
                                     final HostPasswordStore keychain, final LoginCallback login, final HostKeyCallback key,
                                     final ProgressListener listener, final TranscriptListener transcript,
                                     final Usage... usage) {
        final LoginConnectionService connect = new LoginConnectionService(login, key, keychain, listener);
        final CertificateStore certificates = CertificateStoreFactory.get();
        return create(connect, transcript, bookmark,
                new KeychainX509TrustManager(CertificateTrustCallbackFactory.get(controller), new DefaultTrustManagerHostnameCallback(bookmark), certificates),
                new KeychainX509KeyManager(CertificateIdentityCallbackFactory.get(controller), bookmark, certificates),
                VaultRegistryFactory.get(keychain, login), usage);
    }

    public static SessionPool create(final ConnectionService connect, final TranscriptListener transcript,
                                     final Host bookmark,
                                     final X509TrustManager trust, final X509KeyManager key,
                                     final VaultRegistry registry,
                                     final Usage... usage) {
        switch(bookmark.getProtocol().getStatefulness()) {
            case stateful:
                if(Arrays.asList(usage).contains(Usage.browser)) {
                    log.info("Create new stateful connection pool for {}", bookmark);
                    final Session<?> session = SessionFactory.create(new Host(bookmark).withCredentials(new Credentials(bookmark.getCredentials())), trust, key);
                    return new StatefulSessionPool(connect, session, transcript, registry);
                }
                // Break through to default pool
                log.info("Create new pooled connection pool for {}", bookmark);
                final HostPreferences preferences = new HostPreferences(bookmark);
                return new DefaultSessionPool(connect, trust, key, registry, transcript, bookmark)
                        .withMinIdle(preferences.getInteger("connection.pool.minidle"))
                        .withMaxIdle(preferences.getInteger("connection.pool.maxidle"))
                        .withMaxTotal(preferences.getInteger("connection.pool.maxtotal"));
            default:
                // Stateless protocol
                log.info("Create new stateless connection pool for {}", bookmark);
                final Session<?> session = SessionFactory.create(bookmark, trust, key);
                return new StatelessSessionPool(connect, session, transcript, registry);
        }
    }

    public enum Usage {
        transfer,
        browser
    }
}
