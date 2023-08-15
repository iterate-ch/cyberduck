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
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class KeychainLoginService implements LoginService {
    private static final Logger log = LogManager.getLogger(KeychainLoginService.class);

    private final HostPasswordStore keychain;

    public KeychainLoginService() {
        this(PasswordStoreFactory.get());
    }

    public KeychainLoginService(final HostPasswordStore keychain) {
        this.keychain = keychain;
    }

    @Override
    public void validate(final Host bookmark, final LoginCallback prompt, final LoginOptions options) throws ConnectionCanceledException, LoginFailureException {
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
            final CredentialsConfigurator configurator = bookmark.getProtocol().getCredentialsFinder();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Auto configure credentials with %s", configurator));
            }
            bookmark.setCredentials(configurator.configure(bookmark));
        }
        if(!credentials.validate(bookmark.getProtocol(), options)) {
            final StringAppender message = new StringAppender();
            if(options.password) {
                message.append(MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(bookmark)));
            }
            if(options.publickey) {
                message.append(LocaleFactory.localizedString(
                        "Select the private key in PEM or PuTTY format", "Credentials"));
            }
            message.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
            this.prompt(bookmark, message.toString(), prompt, options);
        }
    }

    /**
     * Prompt for credentials if not found in keychain
     *
     * @param bookmark Host configuration
     * @param message  Message in prompt
     * @param prompt   Login prompt
     * @param options  Available login options for protocol
     * @return True if credentials have been updated
     * @throws LoginCanceledException Prompt canceled by user
     */
    public boolean prompt(final Host bookmark, final String message, final LoginCallback prompt, final LoginOptions options) throws ConnectionCanceledException {
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
        if(options.oauth) {
            prompt.warn(bookmark, LocaleFactory.localizedString("Login failed", "Credentials"), message,
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    LocaleFactory.localizedString("Cancel", "Localizable"), null);
            log.warn(String.format("Reset OAuth tokens for %s", bookmark));
            credentials.setOauth(OAuthTokens.EMPTY);
        }
        return options.password || options.token || options.oauth;
    }

    @Override
    public boolean authenticate(final Proxy proxy, final Session session, final ProgressListener listener,
                                final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Host bookmark = session.getHost();
        final Credentials credentials = bookmark.getCredentials();
        if(credentials.isPasswordAuthentication()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                    credentials.getUsername()));
        }
        else if(credentials.isOAuthAuthentication()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                    credentials.getOauth().getAccessToken()));
        }
        else if(credentials.isPublicKeyAuthentication()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                    credentials.getIdentity().getName()));
        }
        else if(credentials.isCertificateAuthentication()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                    credentials.getCertificate()));
        }
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", session));
            }
            session.login(proxy, prompt, cancel);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for %s", session));
            }
            listener.message(LocaleFactory.localizedString("Login successful", "Credentials"));
            this.save(bookmark);
            return true;
        }
        catch(LoginFailureException e) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login failed for %s", session));
            }
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            credentials.setPassed(false);
            final LoginOptions options = new LoginOptions(bookmark.getProtocol());
            if(this.prompt(bookmark, e.getDetail(), prompt, options)) {
                // Retry
                return false;
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Reset credentials for %s", bookmark));
            }
            // No updated credentials. Nullify input
            switch(session.getHost().getProtocol().getStatefulness()) {
                case stateless:
                    credentials.reset();
            }
            throw e;
        }
    }

    public void save(final Host bookmark) {
        final Credentials credentials = bookmark.getCredentials();
        if(credentials.isSaved()) {
            // Write credentials to keychain
            try {
                keychain.save(bookmark);
            }
            catch(LocalAccessDeniedException e) {
                log.error(String.format("Failure saving credentials for %s in keychain. %s", bookmark, e));
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip writing credentials for bookmark %s", bookmark.getHostname()));
            }
        }
        // Flag for successful authentication
        credentials.setPassed(true);
        // Nullify password and tokens
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset credentials for %s", bookmark));
        }
        switch(bookmark.getProtocol().getStatefulness()) {
            case stateless:
                credentials.reset();
        }
    }
}
