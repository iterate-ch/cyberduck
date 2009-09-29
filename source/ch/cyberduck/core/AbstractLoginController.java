package ch.cyberduck.core;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractLoginController implements LoginController {
    private static Logger log = Logger.getLogger(AbstractLoginController.class);

    /**
     * Password already found in keychain
     */
    private boolean persisted;

    /**
     * Check the credentials for validity and prompt the user for the password if not found
     * in the login keychain
     *
     * @param host See Host#getCredentials
     * @throws LoginCanceledException
     */
    public void check(final Host host) throws LoginCanceledException {
        this.check(host, null);
    }

    /**
     * Check the credentials for validity and prompt the user for the password if not found
     * in the login keychain
     *
     * @param host    See Host#getCredentials
     * @param message Additional message displayed in the password prompt
     */
    public void check(final Host host, String message)
            throws LoginCanceledException {

        StringBuffer reason = new StringBuffer();
        if(StringUtils.isNotBlank(message)) {
            reason.append(message).append(". ");
        }
        final Credentials credentials = host.getCredentials();
        if(credentials.isPublicKeyAuthentication()) {
            return;
        }
        if(!credentials.isValid()) {
            final String title = Locale.localizedString("Login with username and password", "Credentials");
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    String passFromKeychain = this.find(host);
                    if(StringUtils.isBlank(passFromKeychain)) {
                        reason.append(Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                        this.prompt(host, title, reason.toString());
                    }
                    else {
                        persisted = true;
                        credentials.setPassword(passFromKeychain);
                    }
                }
                else {
                    reason.append(Locale.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials"));
                    this.prompt(host, title, reason.toString());
                }
            }
            else {
                reason.append(Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                this.prompt(host, title, reason.toString());
            }
        }
    }

    public void success(final Host host) {
        if(persisted) {
            log.info("Password already persisted in Keychain");
            return;
        }
        this.save(host);
    }

    public void fail(final Host host, final String reason) throws LoginCanceledException {
        persisted = false;
        this.prompt(host, Locale.localizedString("Login failed", "Credentials"), reason);
    }

    /**
     * @param host
     * @return the password fetched from the keychain or null if it was not found
     */
    public String find(final Host host) {
        if(log.isInfoEnabled()) {
            log.info("Fetching password from Keychain:" + host);
        }
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        if(StringUtils.isEmpty(host.getCredentials().getUsername())) {
            log.warn("No username given");
            return null;
        }
        final String p = KeychainFactory.instance().getPassword(host.getProtocol().getScheme(), host.getPort(),
                host.getHostname(), host.getCredentials().getUsername());
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info("Password not found in Keychain:" + host);
            }
        }
        return p;
    }

    /**
     * Adds the password to the login keychain
     *
     * @param host
     */
    protected void save(final Host host) {
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        if(StringUtils.isEmpty(host.getCredentials().getUsername())) {
            log.warn("No username given");
            return;
        }
        if(StringUtils.isEmpty(host.getCredentials().getPassword())) {
            log.warn("No password given");
            return;
        }
        if(host.getCredentials().isAnonymousLogin()) {
            log.info("Do not write anonymous credentials to Keychain");
            return;
        }
        if(!host.getCredentials().usesKeychain()) {
            log.info("Do not write credentials to Keychain");
            return;
        }
        if(log.isInfoEnabled()) {
            log.info("Add Password to Keychain:" + host);
        }
        KeychainFactory.instance().addPassword(host.getProtocol().getScheme(), host.getPort(),
                host.getHostname(), host.getCredentials().getUsername(), host.getCredentials().getPassword());
    }

}
