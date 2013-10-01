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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class KeychainLoginService implements LoginService {
    private static final Logger log = Logger.getLogger(KeychainLoginService.class);

    private LoginController controller;
    private HostPasswordStore keychain;

    public KeychainLoginService(final LoginController prompt, final HostPasswordStore keychain) {
        this.controller = prompt;
        this.keychain = keychain;
    }

    /**
     * Attempts to login using the credentials provided from the login controller. Repeat failed
     * login attempts until canceled by the user.
     *
     * @param session Session
     * @param cache   Directory listing cache
     */
    @Override
    public void login(final Session session, final Cache cache, final ProgressListener listener) throws BackgroundException {
        final Host bookmark = session.getHost();
        this.validate(bookmark,
                MessageFormat.format(LocaleFactory.localizedString("Login {0} with username and password", "Credentials"), bookmark.getHostname()));
        if(session.alert()) {
            // Warning if credentials are sent plaintext.
            controller.warn(bookmark.getProtocol(), MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"),
                    bookmark.getProtocol().getName()),
                    MessageFormat.format(LocaleFactory.localizedString("{0} will be sent in plaintext.", "Credentials"),
                            bookmark.getCredentials().getPasswordPlaceholder()),
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    LocaleFactory.localizedString("Disconnect", "Credentials"),
                    String.format("connection.unsecure.%s", bookmark.getHostname()));
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Authenticating as {0}", "Status"),
                bookmark.getCredentials().getUsername()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", bookmark));
            }
            session.login(keychain, controller, cache);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for session %s", session));
            }
            listener.message(LocaleFactory.localizedString("Login successful", "Credentials"));
            // Write credentials to keychain
            keychain.save(bookmark);
            // Reset password in memory
            bookmark.getCredentials().setPassword(null);
        }
        catch(LoginFailureException e) {
            listener.message(LocaleFactory.localizedString("Login failed", "Credentials"));
            try {
                controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                        LocaleFactory.localizedString("Login failed", "Credentials"), e.getDetail(),
                        new LoginOptions(bookmark.getProtocol()));
            }
            catch(LoginCanceledException c) {
                // Reset password in memory
                bookmark.getCredentials().setPassword(null);
                throw c;
            }
            this.login(session, cache, listener);
        }
    }

    public void validate(final Host bookmark, final String title) throws LoginCanceledException {
        this.validate(bookmark, title, new LoginOptions(bookmark.getProtocol()));
    }

    public void validate(final Host bookmark, final String title, final LoginOptions options) throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validate login credentials for %s", bookmark));
        }
        if(!bookmark.getCredentials().validate(bookmark.getProtocol(), options)
                || bookmark.getCredentials().isPublicKeyAuthentication()) {
            // Lookup password if missing. Always lookup password for public key authentication. See #5754.
            if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    final String password = keychain.find(bookmark);
                    if(StringUtils.isBlank(password)) {
                        if(!bookmark.getCredentials().isPublicKeyAuthentication()) {
                            controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                                    title, LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"),
                                    options);
                        }
                        // We decide later if the key is encrypted and a password must be known to decrypt.
                    }
                    else {
                        bookmark.getCredentials().setPassword(password);
                        // No need to reinsert found password to the keychain.
                        bookmark.getCredentials().setSaved(false);
                    }
                }
                else {
                    if(!bookmark.getCredentials().isPublicKeyAuthentication()) {
                        controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                                title,
                                LocaleFactory.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials"), options);
                    }
                    // We decide later if the key is encrypted and a password must be known to decrypt.
                }
            }
            else {
                controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                        title,
                        LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"), options);
            }
        }
    }
}
