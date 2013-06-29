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
    private static final Logger log = Logger.getLogger(AbstractLoginController.class);

    private PasswordStore store;

    protected AbstractLoginController(final PasswordStore store) {
        this.store = store;
    }

    @Override
    public void warn(final String title, final String message,
                     final String continueButton, final String disconnectButton,
                     final String preference)
            throws LoginCanceledException {
        // No warning by default
    }

    @Override
    public void check(final Host host, final String title, final String message)
            throws LoginCanceledException {
        final LoginOptions options = new LoginOptions();
        options.publickey = host.getProtocol().equals(Protocol.SFTP);
        options.anonymous = host.getProtocol().isAnonymousConfigurable();
        this.check(host, title, message, options);
    }

    @Override
    public void check(final Host host, final String title, final String message, final LoginOptions options)
            throws LoginCanceledException {

        final Credentials credentials = host.getCredentials();

        final StringBuilder reason = new StringBuilder();
        if(StringUtils.isNotBlank(message)) {
            reason.append(message).append(". ");
        }
        if(!credentials.validate(host.getProtocol()) || credentials.isPublicKeyAuthentication()) {
            // Lookup password if missing. Always lookup password for public key authentication. See #5754.
            if(StringUtils.isNotBlank(credentials.getUsername())) {
                if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
                    final String password = this.find(host);
                    if(StringUtils.isBlank(password)) {
                        if(!credentials.isPublicKeyAuthentication()) {
                            reason.append(Locale.localizedString(
                                    "No login credentials could be found in the Keychain", "Credentials")).append(".");
                            this.prompt(host.getProtocol(), credentials, title, reason.toString(), options);
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
                        reason.append(Locale.localizedString(
                                "The use of the Keychain is disabled in the Preferences", "Credentials")).append(".");
                        this.prompt(host.getProtocol(), credentials, title, reason.toString(), options);
                    }
                    // We decide later if the key is encrypted and a password must be known to decrypt.
                }
            }
            else {
                reason.append(Locale.localizedString(
                        "No login credentials could be found in the Keychain", "Credentials")).append(".");
                this.prompt(host.getProtocol(), credentials, title, reason.toString(), options);
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
            p = store.getPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath());
            if(null == p) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                p = store.getPassword("SSH", credentials.getIdentity().getAbsolute());
            }
            if(null == p) {
                // Backward compatibility
                p = store.getPassword("SSHKeychain", credentials.getIdentity().getAbbreviatedPath());
            }
        }
        else {
            p = store.getPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername());
        }
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", host));
            }
        }
        return p;
    }

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials,
                       final String title, final String reason) throws LoginCanceledException {
        final LoginOptions options = new LoginOptions();
        options.publickey = protocol.equals(Protocol.SFTP);
        options.anonymous = protocol.isAnonymousConfigurable();
        this.prompt(protocol, credentials, title, reason, options);
    }
}
