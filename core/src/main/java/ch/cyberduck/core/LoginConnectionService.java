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
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Date;

public class LoginConnectionService implements ConnectionService {
    private static final Logger log = Logger.getLogger(LoginConnectionService.class);

    private final Resolver resolver = new Resolver();
    private final HostKeyCallback key;
    private final ProgressListener listener;
    /**
     * Proxy credentials prompt
     */
    private final LoginCallback prompt;
    private final ProxyFinder proxy;
    private final LoginService login;

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener) {
        this(new KeychainLoginService(prompt, keychain), prompt, key, listener);
    }

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener,
                                  final ProxyFinder proxy) {
        this(new KeychainLoginService(prompt, keychain), prompt, key, listener, proxy);
    }

    public LoginConnectionService(final LoginService login,
                                  final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final ProgressListener listener) {
        this(login, prompt, key, listener, ProxyFactory.get());
    }

    public LoginConnectionService(final LoginService login,
                                  final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final ProgressListener listener,
                                  final ProxyFinder proxy) {
        this.login = login;
        this.prompt = prompt;
        this.proxy = proxy;
        this.key = key;
        this.listener = listener;
    }

    @Override
    public boolean check(final Session<?> session, final Cache<Path> cache, final CancelCallback callback) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(bookmark.getProtocol().isHostnameConfigurable() && StringUtils.isBlank(bookmark.getHostname())) {
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
        synchronized(login) {
            login.validate(bookmark,
                MessageFormat.format(LocaleFactory.localizedString(
                    "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(bookmark)),
                new LoginOptions(bookmark.getProtocol()));
        }
        this.connect(session, cache, callback);
        return true;
    }

    @Override
    public void close(final Session<?> session) {
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
    public void connect(final Session<?> session, final Cache<Path> cache, final CancelCallback callback) throws BackgroundException {
        if(session.isConnected()) {
            this.close(session);
        }
        final Host bookmark = session.getHost();

        // Try to resolve the hostname first
        final HostnameConfigurator configurator = HostnameConfiguratorFactory.get(bookmark.getProtocol());
        final String hostname = configurator.getHostname(bookmark.getHostname());
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Resolving {0}", "Status"),
            hostname));
        final Proxy proxy = this.proxy.find(bookmark);
        if(proxy == Proxy.DIRECT) {
            // Only try to resolve target hostname if direct connection
            try {
                resolver.resolve(hostname, callback);
            }
            catch(ResolveFailedException e) {
                log.warn(String.format("DNS resolver failed for %s", hostname));
                throw e;
            }
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Opening {0} connection to {1}", "Status"),
            bookmark.getProtocol().getName(), hostname));

        // The IP address could successfully be determined
        session.open(proxy, key, prompt);

        listener.message(MessageFormat.format(LocaleFactory.localizedString("{0} connection opened", "Status"),
            bookmark.getProtocol().getName()));

        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());

        try {
            this.authenticate(proxy, session, cache, callback);
        }
        catch(BackgroundException e) {
            this.close(session);
            throw e;
        }
    }

    private void authenticate(final Proxy proxy, final Session session, final Cache<Path> cache, final CancelCallback callback) throws BackgroundException {
        if(!login.authenticate(proxy, session, cache, listener, callback)) {
            if(session.isConnected()) {
                // Next attempt with updated credentials
                this.authenticate(proxy, session, cache, callback);
            }
            else {
                // Reconnect and next attempt with updated credentials
                this.connect(session, cache, callback);
            }
        }
    }
}
