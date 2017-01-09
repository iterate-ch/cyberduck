package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginConnectionService implements ConnectionService {
    private static final Logger log = Logger.getLogger(LoginConnectionService.class);

    private final HostKeyCallback key;

    private final ProgressListener listener;

    private final TranscriptListener transcript;

    private final Resolver resolver
            = new Resolver();

    private final ProxyFinder proxy;

    private final LoginService login;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private final NotificationService notification;

    private final AtomicBoolean canceled
            = new AtomicBoolean();

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript) {
        this(new KeychainLoginService(prompt, keychain), key,
                listener, transcript);
    }

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript,
                                  final ProxyFinder proxy) {
        this(new KeychainLoginService(prompt, keychain), key, listener, transcript, proxy);
    }

    public LoginConnectionService(final LoginService login,
                                  final HostKeyCallback key,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript) {
        this(login, key, listener, transcript, ProxyFactory.get());
    }

    public LoginConnectionService(final LoginService login,
                                  final HostKeyCallback key,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript,
                                  final ProxyFinder proxy) {
        this(login, key, listener, transcript, proxy, NotificationServiceFactory.get());
    }

    public LoginConnectionService(final LoginService login,
                                  final HostKeyCallback key,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript,
                                  final ProxyFinder proxy,
                                  final NotificationService notification) {
        this.login = login;
        this.proxy = proxy;
        this.key = key;
        this.listener = listener;
        this.transcript = transcript;
        this.notification = notification;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @param session Session
     * @param cache   Cache
     * @return True if new connection was opened. False if connection is reused.
     * @throws BackgroundException If opening connection fails
     */
    @Override
    public boolean check(final Session<?> session, final Cache<Path> cache) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(StringUtils.isBlank(bookmark.getHostname())) {
            throw new ConnectionCanceledException();
        }
        if(session.isConnected()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip opening connection for session %s", session));
            }
            // Connection already open
            return false;
        }
        // Obtain password from keychain or prompt
        login.validate(bookmark,
                MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), bookmark.getHostname()),
                new LoginOptions(bookmark.getProtocol()));
        this.connect(session, cache);
        return true;
    }

    @Override
    public boolean check(final Session<?> session, final Cache<Path> cache, final BackgroundException failure) throws BackgroundException {
        if(null == failure) {
            return this.check(session, cache);
        }
        if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
            this.close(session);
        }
        return this.check(session, cache);
    }

    private void close(final Session<?> session) {
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
                session.getHost().getHostname()));
        try {
            // Close the underlying socket first
            session.interrupt();
        }
        catch(BackgroundException e) {
            log.warn(String.format("Ignore failure closing connection %s", e.getMessage()));
        }
    }

    @Override
    public void connect(final Session<?> session, final Cache<Path> cache) throws BackgroundException {
        if(session.isConnected()) {
            this.close(session);
        }
        final Host bookmark = session.getHost();

        // Try to resolve the hostname first
        final HostnameConfigurator configurator = HostnameConfiguratorFactory.get(bookmark.getProtocol());
        final String hostname = configurator.getHostname(bookmark.getHostname());
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Resolving {0}", "Status"),
                hostname));
        if(proxy.find(bookmark) == Proxy.DIRECT) {
            // Only try to resolve target hostname if direct connection
            try {
                resolver.resolve(hostname);
            }
            catch(ResolveFailedException e) {
                log.warn(String.format("DNS resolver failed for %s", hostname));
                throw e;
            }
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Opening {0} connection to {1}", "Status"),
                bookmark.getProtocol().getName(), hostname));

        // The IP address could successfully be determined
        session.withListener(transcript).open(key);

        listener.message(MessageFormat.format(LocaleFactory.localizedString("{0} connection opened", "Status"),
                bookmark.getProtocol().getName()));

        // New connection opened
        notification.notify("Connection opened", bookmark.getHostname());

        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());

        try {
            this.authenticate(session, cache);
        }
        catch(BackgroundException e) {
            this.close(session);
            throw e;
        }
    }

    private void authenticate(final Session session, final Cache<Path> cache) throws BackgroundException {
        try {
            login.authenticate(session, cache, listener, new CancelCallback() {
                @Override
                public void verify() throws ConnectionCanceledException {
                    if(canceled.get()) {
                        throw new LoginCanceledException();
                    }
                }
            });
        }
        catch(LoginFailureException e) {
            if(session.isConnected()) {
                // Next attempt with updated credentials
                this.authenticate(session, cache);
            }
            else {
                // Reconnect and next attempt with updated credentials
                this.connect(session, cache);
            }
        }
    }

    @Override
    public void cancel() {
        canceled.set(true);
        resolver.cancel();
    }
}