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

    public void warn(String title, String message, String preference) throws LoginCanceledException {
        this.warn(title, message, Locale.localizedString("Continue", "Credentials"),
                Locale.localizedString("Disconnect", "Credentials"), preference);
    }

    public abstract void warn(String title, String message, String defaultButton, String otherButton, String preference)
            throws LoginCanceledException;

    /**
     * Check the credentials for validity and prompt the user for the password if not found
     * in the login keychain
     *
     * @param host    See Host#getCredentials
     * @param message Additional message displayed in the password prompt
     * @throws LoginCanceledException
     */
    public void check(final Host host, String title, String message)
            throws LoginCanceledException {

        final Credentials credentials = host.getCredentials();

        StringBuilder reason = new StringBuilder();
        if(StringUtils.isNotBlank(message)) {
            reason.append(message).append(". ");
        }
        if(!credentials.validate(host.getProtocol())) {
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    String saved = KeychainFactory.instance().find(host);
                    if(StringUtils.isBlank(saved)) {
                        if(credentials.isPublicKeyAuthentication()) {
                            ;
                            // We decide later if the key is encrypted and a password must be known to decrypt.
                        }
                        else {
                            reason.append(Locale.localizedString(
                                    "No login credentials could be found in the Keychain", "Credentials")).append(".");
                            this.prompt(host.getProtocol(), credentials, title, reason.toString());
                        }
                    }
                    else {
                        credentials.setPassword(saved);
                        // No need to reinsert found password to the keychain.
                        credentials.setUseKeychain(false);
                    }
                }
                else {
                    reason.append(Locale.localizedString(
                            "The use of the Keychain is disabled in the Preferences", "Credentials")).append(".");
                    this.prompt(host.getProtocol(), credentials, title, reason.toString());
                }
            }
            else {
                reason.append(Locale.localizedString(
                        "No login credentials could be found in the Keychain", "Credentials")).append(".");
                ;
                this.prompt(host.getProtocol(), credentials, title, reason.toString());
            }
        }
    }

    public void fail(Protocol protocol, Credentials credentials, String reason) throws LoginCanceledException {
        this.prompt(protocol, credentials,
                Locale.localizedString("Login failed", "Credentials"), reason);
    }

    public void success(Host host) {
        Credentials credentials = host.getCredentials();
        if(credentials.isAnonymousLogin()) {
            log.info("Do not write anonymous credentials to Keychain");
            return;
        }
        if(!credentials.isUseKeychain()) {
            log.info("Do not write credentials to Keychain");
            return;
        }
        KeychainFactory.instance().save(host);
    }

    /**
     * Display login failure with a prompt to enter the username and password.
     *
     * @param protocol
     * @param credentials
     * @throws LoginCanceledException
     */
    public void fail(Protocol protocol, Credentials credentials)
            throws LoginCanceledException {
        this.prompt(protocol, credentials,
                Locale.localizedString("Login failed", "Credentials"),
                Locale.localizedString("Login with username and password", "Credentials"));
    }

    public void prompt(final Protocol protocol, final Credentials credentials,
                       final String title, final String reason) throws LoginCanceledException {
        this.prompt(protocol, credentials, title, reason, Preferences.instance().getBoolean("connection.login.useKeychain"),
                protocol.equals(Protocol.SFTP), true);
    }

    public abstract void prompt(Protocol protocol, Credentials credentials,
                                String title, String reason,
                                boolean enableKeychain, boolean enablePublicKey, boolean enableAnonymous) throws LoginCanceledException;
}
