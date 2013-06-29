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

import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class LoginService {
    private static final Logger log = Logger.getLogger(LoginService.class);

    private LoginController prompt;
    private PasswordStore keychain;
    private ProgressListener listener;

    public LoginService(final LoginController prompt, final PasswordStore keychain,
                        final ProgressListener listener) {
        this.prompt = prompt;
        this.keychain = keychain;
        this.listener = listener;
    }

    /**
     * Attempts to login using the credentials provided from the login controller. Repeat failed
     * login attempts until canceled by the user.
     *
     * @param session Session
     */
    public void login(final Session session) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt authentication for session %s", session));
        }
        session.prompt(prompt);
        if(this.alert(session)) {
            session.warn(prompt);
        }
        final Host bookmark = session.getHost();
        listener.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                bookmark.getCredentials().getUsername()));
        try {
            session.login(prompt);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for session %s", session));
            }
            listener.message(Locale.localizedString("Login successful", "Credentials"));
            // Write credentials to keychain
            this.save(bookmark);
        }
        catch(LoginFailureException e) {
            listener.message(Locale.localizedString("Login failed", "Credentials"));
            prompt.prompt(bookmark.getProtocol(), bookmark.getCredentials(), Locale.localizedString("Login failed", "Credentials"), e.getDetail());
            this.login(session);
        }
    }

    private boolean alert(final Session session) {
        final Host bookmark = session.getHost();
        if(bookmark.getProtocol().isSecure()) {
            return false;
        }
        if(bookmark.getCredentials().isAnonymousLogin()) {
            return false;
        }
        if(Preferences.instance().getBoolean(String.format("connection.unsecure.%s", bookmark.getHostname()))) {
            return false;
        }
        return Preferences.instance().getBoolean(
                String.format("connection.unsecure.warning.%s", bookmark.getProtocol().getScheme()));
    }

    /**
     * Adds the password to the login keychain
     *
     * @param host Hostname
     * @see ch.cyberduck.core.Host#getCredentials()
     */
    protected void save(final Host host) {
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = host.getCredentials();
        if(!credentials.isSaved()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip writing credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn(String.format("No username in credentials for host %s", host.getHostname()));
            return;
        }
        if(StringUtils.isEmpty(credentials.getPassword())) {
            log.warn(String.format("No password in credentials for host %s", host.getHostname()));
            return;
        }
        if(credentials.isAnonymousLogin()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Do not write anonymous credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Add password for host %s", host));
        }
        if(credentials.isPublicKeyAuthentication()) {
            keychain.addPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                    credentials.getPassword());
        }
        else {
            keychain.addPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
    }
}
