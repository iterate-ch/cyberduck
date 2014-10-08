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
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Id$
 */
public class LoginConnectionService implements ConnectionService {
    private static final Logger log = Logger.getLogger(LoginConnectionService.class);

    private HostKeyCallback key;

    private ProgressListener listener;

    private TranscriptListener transcript;

    private Resolver resolver;

    private LoginService login;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private AtomicBoolean canceled
            = new AtomicBoolean();

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript) {
        this(key,
                new KeychainLoginService(prompt, keychain),
                new Resolver(),
                listener, transcript);
    }

    public LoginConnectionService(final HostKeyCallback key,
                                  final LoginService login,
                                  final Resolver resolver,
                                  final ProgressListener listener,
                                  final TranscriptListener transcript) {
        this.key = key;
        this.listener = listener;
        this.resolver = resolver;
        this.login = login;
        this.transcript = transcript;
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
    public boolean check(final Session session, final Cache<Path> cache) throws BackgroundException {
        if(StringUtils.isBlank(session.getHost().getHostname())) {
            throw new ConnectionCanceledException();
        }
        if(session.isConnected()) {
            // Connection already open
            return false;
        }
        this.connect(session, cache);
        return true;
    }

    @Override
    public boolean check(final Session session, final Cache<Path> cache, final BackgroundException failure) throws BackgroundException {
        if(null == failure) {
            return this.check(session, cache);
        }
        if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
            this.close(session, cache);
            this.connect(session, cache);
            return true;
        }
        return this.check(session, cache);
    }

    private void close(final Session session, final Cache cache) {
        try {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
                    session.getHost().getHostname()));
            // Close the underlying socket first
            session.interrupt();
            cache.clear();
        }
        catch(BackgroundException e) {
            log.warn(String.format("Ignore failure closing connection %s", e.getMessage()));
        }
    }

    @Override
    public void connect(final Session session, final Cache<Path> cache) throws BackgroundException {
        if(session.isConnected()) {
            this.close(session, cache);
        }
        final Host bookmark = session.getHost();

        // Try to resolve the hostname first
        final HostnameConfigurator configurator = HostnameConfiguratorFactory.get(bookmark.getProtocol());
        final String hostname = configurator.getHostname(bookmark.getHostname());
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Resolving {0}", "Status"),
                hostname));
        try {
            resolver.resolve(hostname);
        }
        catch(ResolveFailedException e) {
            log.warn(String.format("DNS resolver failed for %s", hostname));
            throw e;
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Opening {0} connection to {1}", "Status"),
                bookmark.getProtocol().getName(), hostname));

        // The IP address could successfully be determined
        session.open(key, transcript);

        listener.message(MessageFormat.format(LocaleFactory.localizedString("{0} connection opened", "Status"),
                bookmark.getProtocol().getName()));

        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());

        try {
            login.login(session, cache, listener, new CancelCallback() {
                @Override
                public void verify() throws ConnectionCanceledException {
                    if(canceled.get()) {
                        throw new LoginCanceledException();
                    }
                }
            });
        }
        catch(BackgroundException e) {
            this.close(session, cache);
            throw e;
        }
    }

    @Override
    public void cancel() {
        canceled.set(true);
        resolver.cancel();
    }
}