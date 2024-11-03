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
import ch.cyberduck.core.proxy.ProxyHostUrlProvider;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Date;

public class LoginConnectionService implements ConnectionService {
    private static final Logger log = LogManager.getLogger(LoginConnectionService.class);

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
        this(new KeychainLoginService(keychain), prompt, key, listener);
    }

    public LoginConnectionService(final LoginCallback prompt,
                                  final HostKeyCallback key,
                                  final HostPasswordStore keychain,
                                  final ProgressListener listener,
                                  final ProxyFinder proxy) {
        this(new KeychainLoginService(keychain), prompt, key, listener, proxy);
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
    public boolean check(final Session<?> session, final CancelCallback callback) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(bookmark.getProtocol().isHostnameConfigurable() && StringUtils.isBlank(bookmark.getHostname())) {
            throw new ConnectionCanceledException();
        }
        if(session.isConnected()) {
            log.debug("Skip opening connection for session {}", session);
            // Connection already open
            return false;
        }
        // Obtain password from keychain or prompt
        synchronized(login) {
            login.validate(bookmark, prompt, new LoginOptions(bookmark.getProtocol()));
        }
        this.connect(session, callback);
        return true;
    }

    @Override
    public void close(final Session<?> session) throws BackgroundException {
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
            session.getHost().getHostname()));
        // Close the underlying socket first
        session.interrupt();
    }

    @Override
    public void connect(final Session<?> session, final CancelCallback cancel) throws BackgroundException {
        if(session.isConnected()) {
            this.close(session);
        }
        final Host bookmark = session.getHost();
        // Try to resolve the hostname first
        final String hostname = HostnameConfiguratorFactory.get(bookmark.getProtocol()).getHostname(bookmark.getHostname());
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Resolving {0}", "Status"), hostname));
        if(proxy.find(new ProxyHostUrlProvider().get(bookmark)) == Proxy.DIRECT) {
            // Only try to resolve target hostname if direct connection
            if(null == JumpHostConfiguratorFactory.get(bookmark.getProtocol()).getJumphost(bookmark.getHostname())) {
                // Do not attempt to resolve hostname that may only be reachable in internal network from jump host
                try {
                    resolver.resolve(hostname, cancel);
                }
                catch(ResolveFailedException e) {
                    log.warn("DNS resolver failed for {}", hostname);
                    throw e;
                }
            }
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Opening {0} connection to {1}", "Status"),
            bookmark.getProtocol().getName(), hostname));
        // The IP address could successfully be determined
        session.open(proxy, key, prompt, cancel);
        listener.message(MessageFormat.format(LocaleFactory.localizedString("{0} connection opened", "Status"),
            bookmark.getProtocol().getName()));
        // Update last accessed timestamp
        bookmark.setTimestamp(new Date());
        // Warning about insecure connection prior authenticating
        if(session.alert(prompt)) {
            // Warning if credentials are sent plaintext.
            prompt.warn(bookmark, MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"),
                bookmark.getProtocol().getName()),
                MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("{0} will be sent in plaintext.", "Credentials"),
                    bookmark.getProtocol().getPasswordPlaceholder()),
                    LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                LocaleFactory.localizedString("Continue", "Credentials"),
                LocaleFactory.localizedString("Disconnect", "Credentials"),
                String.format("connection.unsecure.%s", bookmark.getHostname()));
        }
        // Login
        try {
            this.authenticate(proxy, session, cancel);
        }
        catch(BackgroundException e) {
            this.close(session);
            throw e;
        }
    }

    private void authenticate(final ProxyFinder proxy, final Session session, final CancelCallback callback) throws BackgroundException {
        if(!login.authenticate(proxy, session, listener, prompt, callback)) {
            if(session.isConnected()) {
                // Next attempt with updated credentials but cancel when prompt is dismissed
                this.authenticate(proxy, session, callback);
            }
            else {
                // Reconnect and next attempt with updated credentials
                this.connect(session, callback);
            }
        }
    }
}
