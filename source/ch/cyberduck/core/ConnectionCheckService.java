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
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @version $Id$
 */
public class ConnectionCheckService {
    private static final Logger log = Logger.getLogger(ConnectionCheckService.class);

    private LoginController prompt;
    private HostKeyController key;
    private PasswordStore keychain;
    private ProgressListener listener;

    public ConnectionCheckService(final LoginController prompt, final HostKeyController key,
                                  final PasswordStore keychain,
                                  final ProgressListener listener) {
        this.prompt = prompt;
        this.key = key;
        this.keychain = keychain;
        this.listener = listener;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @param session Session
     * @throws BackgroundException If opening connection fails
     */
    public void check(final Session session) throws BackgroundException {
        if(!session.isConnected()) {
            if(StringUtils.isBlank(session.getHost().getHostname())) {
                if(StringUtils.isBlank(session.getHost().getProtocol().getDefaultHostname())) {
                    throw new ConnectionCanceledException();
                }
                // If hostname is missing update with default
                session.getHost().setHostname(session.getHost().getProtocol().getDefaultHostname());
            }
            this.connect(session);
        }
        else {
            // The session is still supposed to be connected
            try {
                // Send a 'no operation command' to make sure the session is alive
                session.noop();
            }
            catch(BackgroundException e) {
                log.warn(String.format("No operation command failed for session %s. Attempt to reopen connection", session));
                // Try to reconnect once more
                this.connect(session);
            }
        }
    }

    private void connect(final Session session) throws BackgroundException {
        if(session.isConnected()) {
            // Close the underlying socket first
            session.interrupt();
        }
        final Host bookmark = session.getHost();

        // Configuring proxy if any
        ProxyFactory.get().configure(bookmark);

        final Resolver resolver = new Resolver(
                HostnameConfiguratorFactory.get(bookmark.getProtocol()).lookup(bookmark.getHostname()));

        listener.message(MessageFormat.format(Locale.localizedString("Resolving {0}", "Status"),
                bookmark.getHostname()));

        // Try to resolve the hostname first
        try {
            session.fireConnectionWillOpenEvent();
            resolver.resolve();
        }
        catch(IOException e) {
            session.fireConnectionDidCloseEvent();
            throw new DefaultIOExceptionMappingService().map(e);
        }

        listener.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                bookmark.getProtocol().getName(), bookmark.getHostname()));

        // The IP address could successfully be determined
        session.open(key);

        GrowlFactory.get().notify("Connection opened", bookmark.getHostname());

        listener.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                bookmark.getProtocol().getName()));

        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());

        try {
            final LoginService login = new LoginService(prompt, keychain);
            login.login(session, listener);

            final HistoryCollection history = HistoryCollection.defaultCollection();
            history.add(new Host(bookmark.getAsDictionary()));

            // Notify changed bookmark
            if(BookmarkCollection.defaultCollection().contains(bookmark)) {
                BookmarkCollection.defaultCollection().collectionItemChanged(bookmark);
            }
        }
        catch(BackgroundException e) {
            session.interrupt();
            throw e;
        }
    }
}