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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @version $Id$
 */
public class LoginConnectionService implements ConnectionService {
    private static final Logger log = Logger.getLogger(LoginConnectionService.class);

    private HostKeyController key;

    private ProgressListener listener;

    private Resolver resolver;

    private LoginService login;

    private Proxy proxy;

    public LoginConnectionService(final LoginController prompt,
                                  final HostKeyController key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener) {
        this(key,
                new KeychainLoginService(prompt, keychain),
                new Resolver(),
                ProxyFactory.get(),
                listener);
    }

    public LoginConnectionService(final HostKeyController key,
                                  final LoginService login,
                                  final Resolver resolver,
                                  final Proxy proxy,
                                  final ProgressListener listener) {
        this.key = key;
        this.listener = listener;
        this.resolver = resolver;
        this.login = login;
        this.proxy = proxy;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @param session Session
     * @return True if new connection was opened. False if connection is reused.
     * @throws BackgroundException If opening connection fails
     */
    @Override
    public boolean check(final Session session, final Cache cache) throws BackgroundException {
        if(!session.isConnected()) {
            if(StringUtils.isBlank(session.getHost().getHostname())) {
                throw new ConnectionCanceledException();
            }
            this.connect(session, cache);
        }
        else {
            // The session is still supposed to be connected
            try {
                // Send a 'no operation command' to make sure the session is alive
                session.noop();
                return false;
            }
            catch(BackgroundException e) {
                log.warn(String.format("No operation command failed for session %s. Attempt to reopen connection", session));
                // Try to reconnect once more
                this.connect(session, cache);
            }
        }
        return true;
    }

    @Override
    public void connect(final Session session, final Cache cache) throws BackgroundException {
        if(session.isConnected()) {
            try {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
                        session.getHost().getHostname()));
                // Close the underlying socket first
                session.interrupt();
            }
            catch(BackgroundException e) {
                log.warn(String.format("Ignore failure closing connection %s", e.getMessage()));
            }
        }
        final Host bookmark = session.getHost();

        // Configuring proxy if any
        proxy.configure(bookmark);

        listener.message(MessageFormat.format(LocaleFactory.localizedString("Resolving {0}", "Status"),
                bookmark.getHostname()));

        // Try to resolve the hostname first
        try {
            resolver.resolve(HostnameConfiguratorFactory.get(bookmark.getProtocol()).getHostname(bookmark.getHostname()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }

        listener.message(MessageFormat.format(LocaleFactory.localizedString("Opening {0} connection to {1}", "Status"),
                bookmark.getProtocol().getName(), bookmark.getHostname()));

        // The IP address could successfully be determined
        session.open(key);

        listener.message(MessageFormat.format(LocaleFactory.localizedString("{0} connection opened", "Status"),
                bookmark.getProtocol().getName()));

        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());

        try {
            login.login(session, cache, listener);
        }
        catch(BackgroundException e) {
            session.interrupt();
            throw e;
        }
    }

    @Override
    public void cancel() {
        resolver.cancel();
    }
}