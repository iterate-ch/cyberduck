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

    private final HostPasswordStore keychain;

    public KeychainLoginService(final HostPasswordStore keychain) {
        this.keychain = keychain;
    }

    @Override
    public void validate(final Host bookmark, final String message, final LoginCallback prompt,
                         final LoginOptions options) throws LoginCanceledException, LoginFailureException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validate login credentials for %s", bookmark));
        }
        final Credentials credentials = bookmark.getCredentials();
        if(credentials.isPublicKeyAuthentication()) {
            if(!credentials.getIdentity().attributes().getPermission().isReadable()) {
                log.warn(String.format("Prompt to select identity file not readable %s", credentials.getIdentity()));
                credentials.setIdentity(prompt.select(credentials.getIdentity()));
            }
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
            if(options.publickey) {
                final String passphrase = keychain.findPrivateKeyPassphrase(bookmark);
                if(StringUtils.isNotBlank(passphrase)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Fetched private key passphrase from keychain for %s", bookmark));
                    }
                    // No need to reinsert found token to the keychain.
                    credentials.setSaved(false);
                    credentials.setIdentityPassphrase(passphrase);
                }
            }
            if(options.oauth) {
                final OAuthTokens tokens = keychain.findOAuthTokens(bookmark);
                if(tokens.validate()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Fetched OAuth token from keychain for %s", bookmark));
                    }
                    // No need to reinsert found token to the keychain.
                    credentials.setSaved(false);
                    credentials.setOauth(tokens);
                }
            }
        }
        if(!credentials.validate(bookmark.getProtocol(), options)) {
            final StringAppender details = new StringAppender();
            details.append(message);
            details.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
            this.prompt(bookmark, details.toString(), prompt, options);
        }
    }

    /**
     * Prompt for credentials if not found in keychain
     *
     * @param bookmark Host configuration
     * @param message  Message in prompt
     * @param prompt   Login prompt
     * @param options  Available login options for protocol
     * @throws LoginCanceledException Prompt canceled by user
     */
    public void prompt(final Host bookmark, final String message, final LoginCallback prompt, final LoginOptions options) throws LoginCanceledException {
        final Credentials credentials = bookmark.getCredentials();
        if(options.password) {
            final Credentials input = prompt.prompt(bookmark, credentials.getUsername(),
                String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                message,
                options);
            credentials.setSaved(input.isSaved());
            credentials.setUsername(input.getUsername());
            credentials.setPassword(input.getPassword());
            credentials.setIdentity(input.getIdentity());
        }
        if(options.token) {
            final Credentials input = prompt.prompt(bookmark,
                LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                message,
                options);
            credentials.setSaved(input.isSaved());
            credentials.setToken(input.getPassword());
        }
    }

    @Override
    public boolean authenticate(final Proxy proxy, final Session session, final ProgressListener listener,
                                final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Host bookmark = session.getHost();
        final Credentials credentials = bookmark.getCredentials();
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
            StringUtils.isEmpty(credentials.getUsername()) ? LocaleFactory.localizedString("Unknown") : credentials.getUsername()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", bookmark));
            }
            session.login(proxy, prompt, cancel);
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
            return true;
        }
        catch(LoginFailureException e) {
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            credentials.setPassed(false);
            final LoginOptions options = new LoginOptions(bookmark.getProtocol());
            if(options.user && options.password) {
                // Default login prompt with username and password input
                final Credentials input = prompt.prompt(bookmark, credentials.getUsername(),
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
                // Retry
                return false;
            }
            else {
                final StringAppender details = new StringAppender();
                details.append(LocaleFactory.localizedString("Login failed", "Credentials"));
                details.append(e.getDetail());
                this.prompt(bookmark, details.toString(), prompt, options);
                // Retry
                return false;
            }
        }
    }
}
