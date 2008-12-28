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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractLoginController implements LoginController {
    private static Logger log = Logger.getLogger(AbstractLoginController.class);

    /**
     * Try to the password from the user or the Keychain
     *
     * @param host
     * @return true if reasonable values have been found localy or in the keychain or the user
     *         was prompted to for the credentials and new values got entered.
     */
    public void check(final Host host)
            throws LoginCanceledException {

        if(host.isPublicKeyAuthentication()) {
            return;
        }
        final Credentials credentials = host.getCredentials();
        if(!credentials.isValid()) {
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    log.info("Searching keychain for password...");
                    String passFromKeychain = credentials.getInternetPasswordFromKeychain(host.getProtocol(), host.getHostname());
                    if(StringUtils.isBlank(passFromKeychain)) {
                        this.prompt(host,
                                NSBundle.localizedString("Login with username and password", "Credentials", ""),
                                NSBundle.localizedString("No login credentials could be found in the Keychain", "Credentials", ""));
                    }
                    else {
                        credentials.setPassword(passFromKeychain);
                    }
                }
                else {
                    this.prompt(host,
                            NSBundle.localizedString("Login with username and password", "Credentials", ""),
                            NSBundle.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials", ""));
                }
            }
            else {
                this.prompt(host,
                        NSBundle.localizedString("Login with username and password", "Credentials", ""),
                        NSBundle.localizedString("No login credentials could be found in the Keychain", "Credentials", ""));
            }
        }
    }

    public void fail(final Host host, final String reason)
            throws LoginCanceledException {
        this.prompt(host, NSBundle.localizedString("Login failed", "Credentials", ""),
                reason);
    }
}
