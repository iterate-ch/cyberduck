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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class KeychainLoginService implements LoginService {
    private static final Logger log = LogManager.getLogger(KeychainLoginService.class);

    private final HostPasswordStore keychain;

    public KeychainLoginService(final HostPasswordStore keychain) {
        this.keychain = keychain;
    }

    @Override
    public void validate(final Host host, final X509KeyManager keys, final LoginCallback prompt, final LoginOptions options) throws ConnectionCanceledException, LoginFailureException {
        log.debug("Validate login credentials for {}", host);
        final Host jumphost = host.getJumphost();
        if(null != jumphost) {
            this.validate(jumphost, keys, prompt, new LoginOptions(jumphost.getProtocol()));
        }
        final Credentials credentials = host.getCredentials();
        if(credentials.isPublicKeyAuthentication()) {
            if(!credentials.getIdentity().attributes().getPermission().isReadable()) {
                log.warn("Prompt to select identity file not readable {}", credentials.getIdentity());
                credentials.setIdentity(prompt.select(credentials.getIdentity()));
            }
        }
        if(options.keychain) {
            log.debug("Lookup credentials in keychain for {}", host);
            if(options.password) {
                if(StringUtils.isBlank(credentials.getPassword())) {
                    final String password = keychain.findLoginPassword(host);
                    if(StringUtils.isNotBlank(password)) {
                        log.info("Fetched password from keychain for {}", host);
                        // No need to reinsert found password to the keychain.
                        credentials.setPassword(password).setSaved(false);
                    }
                }
            }
            if(options.token) {
                if(StringUtils.isBlank(credentials.getToken())) {
                    final String token = keychain.findLoginToken(host);
                    if(StringUtils.isNotBlank(token)) {
                        log.info("Fetched token from keychain for {}", host);
                        // No need to reinsert found token to the keychain.
                        credentials.setToken(token).setSaved(false);
                    }
                }
            }
            if(options.publickey) {
                final String passphrase = keychain.findPrivateKeyPassphrase(host);
                if(StringUtils.isNotBlank(passphrase)) {
                    log.info("Fetched private key passphrase from keychain for {}", host);
                    // No need to reinsert found token to the keychain.
                    credentials.setIdentityPassphrase(passphrase).setSaved(false);
                }
            }
            if(options.oauth) {
                final OAuthTokens tokens = keychain.findOAuthTokens(host);
                if(tokens.validate()) {
                    log.info("Fetched OAuth tokens {} from keychain for {}", tokens, host);
                    // No need to reinsert found token to the keychain.
                    credentials.setOauth(tokens).setSaved(tokens.isExpired());
                }
            }
            if(options.certificate) {
                final String alias = host.getCredentials().getCertificate();
                if(StringUtils.isNotBlank(alias)) {
                    if(keys != null) {
                        if(null == keys.getPrivateKey(alias)) {
                            log.warn("No private key found for alias {} in keychain", alias);
                            throw new LoginFailureException(LocaleFactory.localizedString("Provide additional login credentials", "Credentials"));
                        }
                    }
                }
            }
        }
        if(!credentials.validate(host.getProtocol(), options)) {
            log.warn("Failed validation of credentials {} with options {}", credentials, options);
            final CredentialsConfigurator configurator = CredentialsConfiguratorFactory.get(host.getProtocol());
            log.debug("Auto configure credentials with {}", configurator);
            final Credentials configuration = configurator.configure(host);
            if(configuration.validate(host.getProtocol(), options)) {
                host.setCredentials(configuration);
                log.info("Auto configured credentials {} for {}", configuration, host);
                return;
            }
            final StringAppender message = new StringAppender();
            if(options.password) {
                message.append(MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(host)));
            }
            if(options.publickey) {
                message.append(LocaleFactory.localizedString(
                        "Select the private key in PEM or PuTTY format", "Credentials"));
            }
            message.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
            this.prompt(host, message.toString(), prompt, options);
        }
        log.debug("Validated credentials {} with options {}", credentials, options);
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
                    LocaleFactory.localizedString("Try Again", "Alert"),
                    LocaleFactory.localizedString("Cancel", "Localizable"), null);
            log.warn("Reset OAuth tokens for {}", bookmark);
            credentials.setOauth(OAuthTokens.EMPTY);
        }
        return options.password || options.token || options.oauth;
    }

    @Override
    public boolean authenticate(final Session<?> session, final ProgressListener listener,
                                final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Host bookmark = session.getHost();
        final Credentials credentials = bookmark.getCredentials();
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                StringUtils.isNotBlank(credentials.getUsername()) ? credentials.getUsername() : LocaleFactory.localizedString("Unknown")));
        try {
            log.debug("Attempt authentication for {}", session);
            session.login(prompt, cancel);
            log.debug("Login successful for {}", session);
            listener.message(LocaleFactory.localizedString("Login successful", "Credentials"));
            this.save(bookmark);
            return true;
        }
        catch(LoginFailureException e) {
            log.debug("Login failed for {}", session);
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            final LoginOptions options = new LoginOptions(bookmark.getProtocol());
            try {
                if(this.prompt(bookmark, e.getDetail(), prompt, options)) {
                    // Retry
                    return false;
                }
            }
            catch(LoginCanceledException c) {
                // Canceled by user
                try {
                    c.initCause(e);
                }
                catch(IllegalArgumentException | IllegalStateException r) {
                    log.warn("Ignore error {} initializing failure {} with cause {}", r, e, c);
                }
                throw c;
            }
            log.debug("Reset credentials for {}", bookmark);
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
            // Write credentials to the password store
            try {
                keychain.save(bookmark);
                if(bookmark.getJumphost() != null) {
                    keychain.save(bookmark.getJumphost());
                }
            }
            catch(AccessDeniedException e) {
                log.error("Failure saving credentials for {} in keychain. {}", bookmark, e);
            }
        }
        else {
            log.info("Skip writing credentials for bookmark {}", bookmark.getHostname());
        }
    }
}
