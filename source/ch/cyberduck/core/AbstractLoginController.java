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

        final Credentials credentials = host.getCredentials();

        StringBuilder reason = new StringBuilder();
        if(StringUtils.isNotBlank(message)) {
            reason.append(message).append(". ");
        }
        if(credentials.isPublicKeyAuthentication()) {
            return;
        }
        if(!credentials.isValid()) {
            final String title = Locale.localizedString("Login with username and password", "Credentials");
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    String passFromKeychain = KeychainFactory.instance().find(host);
                    if(StringUtils.isBlank(passFromKeychain)) {
                        reason.append(Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                        this.prompt(host.getProtocol(), credentials, title, reason.toString());
                    }
                    else {
                        credentials.setPassword(passFromKeychain);
                        credentials.setUseKeychain(false);
                    }
                }
                else {
                    reason.append(Locale.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials"));
                    this.prompt(host.getProtocol(), credentials, title, reason.toString());
                }
            }
            else {
                reason.append(Locale.localizedString("No login credentials could be found in the Keychain", "Credentials"));
                this.prompt(host.getProtocol(), credentials, title, reason.toString());
            }
        }
    }

    /**
     * @param host
     */
    public void success(final Host host) {
        KeychainFactory.instance().save(host);
    }

    public void fail(Protocol protocol, Credentials credentials, final String reason) throws LoginCanceledException {
        this.prompt(protocol, credentials, Locale.localizedString("Login failed", "Credentials"), reason);
    }
}
