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

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class AbstractLoginController implements LoginController {
    private static Logger log = Logger.getLogger(AbstractLoginController.class);

    /**
     * Try to the password from the user or the Keychain
     *
     * @return true if reasonable values have been found localy or in the keychain or the user
     *         was prompted to for the credentials and new values got entered.
     */
    public void check(final Credentials credentials, final Protocol protocol, final String hostname)
            throws LoginCanceledException {
        if(!credentials.isValid()) {
            if(StringUtils.hasText(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    log.info("Searching keychain for password...");
                    String passFromKeychain = credentials.getInternetPasswordFromKeychain(protocol, hostname);
                    if(!StringUtils.hasText(passFromKeychain)) {
                        this.prompt(protocol, credentials,
                                NSBundle.localizedString("Login with username and password", "Credentials", ""),
                                NSBundle.localizedString("No login credentials could be found in the Keychain", "Credentials", ""));
                    }
                    else {
                        credentials.setPassword(passFromKeychain);
                    }
                }
                else {
                    this.prompt(protocol, credentials,
                            NSBundle.localizedString("Login with username and password", "Credentials", ""),
                            NSBundle.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials", ""));
                }
            }
            else {
                this.prompt(protocol, credentials,
                        NSBundle.localizedString("Login with username and password", "Credentials", ""),
                        NSBundle.localizedString("No login credentials could be found in the Keychain", "Credentials", ""));
            }
        }
    }

    public void fail(final Protocol protocol, final Credentials credentials, final String reason)
            throws LoginCanceledException {
        this.prompt(protocol, credentials, NSBundle.localizedString("Login failed", "Credentials", ""),
                reason);
    }
}
