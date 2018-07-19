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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class KeychainLoginService implements LoginService {
    private static final Logger log = Logger.getLogger(KeychainLoginService.class);

    private final LoginCallback callback;
    private final HostPasswordStore keychain;

    public KeychainLoginService(final LoginCallback prompt, final HostPasswordStore keychain) {
        this.callback = prompt;
        this.keychain = keychain;
    }

    @Override
    public void validate(final Host bookmark, final String message, final LoginOptions options) throws LoginCanceledException, LoginFailureException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validate login credentials for %s", bookmark));
        }
        final Credentials credentials = bookmark.getCredentials();
        if(credentials.isPublicKeyAuthentication()) {
            if(!credentials.getIdentity().attributes().getPermission().isReadable()) {
                log.warn(String.format("Prompt to select identity file not readable %s", credentials.getIdentity()));
                credentials.setIdentity(callback.select(credentials.getIdentity()));
            }
        }
        if(!credentials.validate(bookmark.getProtocol(), options)) {
            // Try auto-configure
            final Credentials auto = CredentialsConfiguratorFactory.get(bookmark.getProtocol()).configure(bookmark, callback);
            credentials.setUsername(auto.getUsername());
            credentials.setPassword(auto.getPassword());
            credentials.setToken(auto.getToken());
        }
        if(options.keychain) {
            if(options.password) {
                if(StringUtils.isBlank(credentials.getPassword())) {
                    final String password = keychain.findLoginPassword(bookmark);
                    if(StringUtils.isNotBlank(password)) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Fetched password from keychain for %s", bookmark));
                        }
                        // No need to reinsert found password to the keychain.
                        credentials.setSaved(false);
                        credentials.setPassword(password);
                    }
                }
            }
            if(options.token) {
                if(StringUtils.isBlank(credentials.getToken())) {
                    final String token = keychain.findLoginToken(bookmark);
                    if(StringUtils.isNotBlank(token)) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Fetched token from keychain for %s", bookmark));
                        }
                        // No need to reinsert found token to the keychain.
                        credentials.setSaved(false);
                        credentials.setToken(token);
                    }
                }
            }
        }
        if(!credentials.validate(bookmark.getProtocol(), options)) {
            if(options.password) {
                final StringAppender appender = new StringAppender();
                appender.append(message);
                appender.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                final Credentials prompt = callback.prompt(bookmark, credentials.getUsername(),
                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                    appender.toString(),
                    options);
                credentials.setSaved(prompt.isSaved());
                credentials.setUsername(prompt.getUsername());
                credentials.setPassword(prompt.getPassword());
                credentials.setIdentity(prompt.getIdentity());
            }
            if(options.token) {
                final Credentials prompt = callback.prompt(bookmark,
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"),
                    options);
                credentials.setSaved(prompt.isSaved());
                credentials.setToken(prompt.getPassword());
            }
        }
    }

    @Override
    public void authenticate(final Proxy proxy, final Session session, final Cache<Path> cache, final ProgressListener listener, final CancelCallback cancel) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(session.alert(callback)) {
            // Warning if credentials are sent plaintext.
            callback.warn(bookmark, MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"),
                bookmark.getProtocol().getName()),
                MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("{0} will be sent in plaintext.", "Credentials"),
                    bookmark.getProtocol().getPasswordPlaceholder()),
                    LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                LocaleFactory.localizedString("Continue", "Credentials"),
                LocaleFactory.localizedString("Disconnect", "Credentials"),
                String.format("connection.unsecure.%s", bookmark.getHostname()));
        }
        final Credentials credentials = bookmark.getCredentials();
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
            StringUtils.isEmpty(credentials.getUsername()) ? LocaleFactory.localizedString("Unknown") : credentials.getUsername()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", bookmark));
            }
            session.login(proxy, keychain, callback, cancel);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for session %s", session));
            }
            listener.message(LocaleFactory.localizedString("Login successful", "Credentials"));
            // Write credentials to keychain
            keychain.save(bookmark);
            // Flag for successful authentication
            credentials.setPassed(true);
            // Nullify password.
            credentials.setPassword(null);
        }
        catch(LoginFailureException e) {
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            final LoginOptions options = new LoginOptions(bookmark.getProtocol());
            if(options.user && options.password) {
                // Default login prompt with username and password input
                final Credentials input = callback.prompt(bookmark, credentials.getUsername(),
                    LocaleFactory.localizedString("Login failed", "Credentials"), e.getDetail(), options);
                credentials.setUsername(input.getUsername());
                credentials.setPassword(input.getPassword());
                credentials.setSaved(input.isSaved());
                if(input.isPublicKeyAuthentication()) {
                    credentials.setIdentity(input.getIdentity());
                }
                if(input.isCertificateAuthentication()) {
                    credentials.setCertificate(input.getCertificate());
                }
            }
            else {
                // Password prompt
                if(options.password) {
                    final Credentials input = callback.prompt(bookmark,
                        LocaleFactory.localizedString("Login failed", "Credentials"), e.getDetail(), options);
                    if(input.isPasswordAuthentication()) {
                        credentials.setPassword(input.getPassword());
                        credentials.setSaved(input.isSaved());
                    }
                }
                else if(options.token) {
                    final Credentials input = callback.prompt(bookmark,
                        LocaleFactory.localizedString("Login failed", "Credentials"), e.getDetail(), options);
                    if(input.isPasswordAuthentication()) {
                        credentials.setToken(input.getPassword());
                        credentials.setSaved(input.isSaved());
                    }
                }
            }
            throw e;
        }
    }
}
