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
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class KeychainLoginService implements LoginService {
    private static final Logger log = Logger.getLogger(KeychainLoginService.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final LoginCallback callback;

    private final HostPasswordStore keychain;

    public KeychainLoginService(final LoginCallback prompt, final HostPasswordStore keychain) {
        this.callback = prompt;
        this.keychain = keychain;
    }

    @Override
    public void authenticate(final Session session, final Cache<Path> cache, final ProgressListener listener,
                             final CancelCallback cancel) throws BackgroundException {
        final Host bookmark = session.getHost();
        final Credentials credentials = bookmark.getCredentials();
        if(session.alert(callback)) {
            // Warning if credentials are sent plaintext.
            callback.warn(bookmark.getProtocol(), MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"),
                    bookmark.getProtocol().getName()),
                    MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("{0} will be sent in plaintext.", "Credentials"),
                            credentials.getPasswordPlaceholder()),
                            LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    LocaleFactory.localizedString("Disconnect", "Credentials"),
                    String.format("connection.unsecure.%s", bookmark.getHostname()));
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                StringUtils.isEmpty(credentials.getUsername()) ? LocaleFactory.localizedString("Unknown") : credentials.getUsername()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", bookmark));
            }
            session.login(keychain, callback, cancel, cache);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for session %s", session));
            }
            listener.message(LocaleFactory.localizedString("Login successful", "Credentials"));
            // Write credentials to keychain
            keychain.save(bookmark);
            // Flag for successful authentication
            credentials.setPassed(true);
        }
        catch(LoginFailureException e) {
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            credentials.setPassed(false);
            callback.prompt(bookmark, credentials,
                    LocaleFactory.localizedString("Login failed", "Credentials"), e.getDetail(),
                    new LoginOptions(bookmark.getProtocol()));
            throw e;
        }
    }

    @Override
    public void validate(final Host bookmark, final String message, final LoginOptions options) throws LoginCanceledException {
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
        if(!credentials.validate(bookmark.getProtocol(), options)
                || credentials.isPublicKeyAuthentication()) {
            // Lookup password if missing. Always lookup password for public key authentication. See #5754.
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(preferences.getBoolean("connection.login.keychain")) {
                    final String password = keychain.find(bookmark);
                    if(StringUtils.isBlank(password)) {
                        if(!credentials.isPublicKeyAuthentication()) {
                            final StringAppender appender = new StringAppender();
                            appender.append(message);
                            appender.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                            callback.prompt(bookmark, credentials,
                                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                                            appender.toString(),
                                            options);
                        }
                        // We decide later if the key is encrypted and a password must be known to decrypt.
                    }
                    else {
                        credentials.setPassword(password);
                        // No need to reinsert found password to the keychain.
                        credentials.setSaved(false);
                    }
                }
                else {
                    if(!credentials.isPublicKeyAuthentication()) {
                        final StringAppender appender = new StringAppender();
                        appender.append(message);
                        appender.append(LocaleFactory.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials"));
                        callback.prompt(bookmark, credentials,
                                String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                                appender.toString(), options);
                    }
                    // We decide later if the key is encrypted and a password must be known to decrypt.
                }
            }
            else {
                log.warn(String.format("Prompt for username to connect to %s", bookmark));
                // Ask for username
                final StringAppender appender = new StringAppender();
                appender.append(message);
                appender.append(LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                callback.prompt(bookmark, credentials,
                        String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                        appender.toString(), options);
            }
        }
        else {
            if(!credentials.isPassed()) {
                if(preferences.getBoolean("connection.login.keychain")) {
                    final String password = keychain.find(bookmark);
                    if(StringUtils.isNotBlank(password)) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Fetched password from keychain for %s", bookmark));
                        }
                        credentials.setPassword(password);
                        // No need to reinsert found password to the keychain.
                        credentials.setSaved(false);
                    }
                }
            }
            else {
                log.warn(String.format("Skip password lookup for previously authenticated connection %s", bookmark));
            }
        }
    }
}
