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

    private LoginController controller;
    private PasswordStore keychain;

    public LoginService(final LoginController prompt, final PasswordStore keychain) {
        this.controller = prompt;
        this.keychain = keychain;
    }

    /**
     * Attempts to login using the credentials provided from the login controller. Repeat failed
     * login attempts until canceled by the user.
     *
     * @param session Session
     */
    public void login(final Session session, final ProgressListener listener) throws BackgroundException {
        final Host bookmark = session.getHost();
        this.validate(bookmark, Locale.localizedString("Login with username and password", "Credentials"));
        if(session.alert()) {
            // Warning if credentials are sent plaintext.
            controller.warn(MessageFormat.format(Locale.localizedString("Unsecured {0} connection", "Credentials"),
                    bookmark.getProtocol().getName()),
                    MessageFormat.format(Locale.localizedString("{0} will be sent in plaintext.", "Credentials"),
                            bookmark.getCredentials().getPasswordPlaceholder()),
                    Locale.localizedString("Continue", "Credentials"),
                    Locale.localizedString("Disconnect", "Credentials"),
                    String.format("connection.unsecure.%s", bookmark.getHostname()));
        }
        listener.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                bookmark.getCredentials().getUsername()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt authentication for %s", bookmark));
            }
            session.login(controller);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Login successful for session %s", session));
            }
            listener.message(Locale.localizedString("Login successful", "Credentials"));
            // Write credentials to keychain
            this.save(bookmark);
        }
        catch(LoginFailureException e) {
            listener.message(Locale.localizedString("Login failed", "Credentials"));
            controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(), Locale.localizedString("Login failed", "Credentials"), e.getDetail());
            this.login(session, listener);
        }
    }

    public void validate(final Host bookmark, final String title) throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validate login credentials for %s", bookmark));
        }
        if(!bookmark.getCredentials().validate(bookmark.getProtocol())
                || bookmark.getCredentials().isPublicKeyAuthentication()) {
            final LoginOptions options = new LoginOptions();
            options.publickey = bookmark.getProtocol().equals(Protocol.SFTP);
            options.anonymous = bookmark.getProtocol().isAnonymousConfigurable();
            // Lookup password if missing. Always lookup password for public key authentication. See #5754.
            if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    final String password = this.find(bookmark);
                    if(StringUtils.isBlank(password)) {
                        if(!bookmark.getCredentials().isPublicKeyAuthentication()) {
                            controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                                    title,
                                    Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"), options);
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
                                Locale.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials"), options);
                    }
                    // We decide later if the key is encrypted and a password must be known to decrypt.
                }
            }
            else {
                controller.prompt(bookmark.getProtocol(), bookmark.getCredentials(),
                        title,
                        Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"), options);
            }
        }
    }

    /**
     * @param host Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    protected String find(final Host host) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching password from keychain for %s", host));
        }
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        Credentials credentials = host.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        String p;
        if(credentials.isPublicKeyAuthentication()) {
            p = keychain.getPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath());
            if(null == p) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                p = keychain.getPassword("SSH", credentials.getIdentity().getAbsolute());
            }
            if(null == p) {
                // Backward compatibility
                p = keychain.getPassword("SSHKeychain", credentials.getIdentity().getAbbreviatedPath());
            }
        }
        else {
            p = keychain.getPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername());
        }
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", host));
            }
        }
        return p;
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
