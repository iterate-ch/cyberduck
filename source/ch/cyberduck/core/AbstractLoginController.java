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

    @Override
    public void warn(String title, String message, String preference) throws LoginCanceledException {
        this.warn(title, message, Locale.localizedString("Continue", "Credentials"),
                Locale.localizedString("Disconnect", "Credentials"), preference);
    }

    @Override
    public void warn(String title, String message, String continueButton, String disconnectButton, String preference)
            throws LoginCanceledException {
        // No warning by default
    }

    @Override
    public void check(final Host host, final String title, final String message)
            throws LoginCanceledException {
        this.check(host, title, message, Preferences.instance().getBoolean("connection.login.useKeychain"),
                host.getProtocol().equals(Protocol.SFTP), host.getProtocol().isAnonymousConfigurable());
    }

    @Override
    public void check(final Host host, final String title, final String message,
                      final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous)
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
                    final String saved = KeychainFactory.get().find(host);
                    if(StringUtils.isBlank(saved)) {
                        if(!credentials.isPublicKeyAuthentication()) {
                            reason.append(Locale.localizedString(
                                    "No login credentials could be found in the Keychain", "Credentials")).append(".");
                            this.prompt(host.getProtocol(), credentials, title, reason.toString(),
                                    enableKeychain, enablePublicKey, enableAnonymous);
                        }
                        // We decide later if the key is encrypted and a password must be known to decrypt.
                    }
                    else {
                        credentials.setPassword(saved);
                        // No need to reinsert found password to the keychain.
                        credentials.setSaved(false);
                    }
                }
                else {
                    if(!credentials.isPublicKeyAuthentication()) {
                        reason.append(Locale.localizedString(
                                "The use of the Keychain is disabled in the Preferences", "Credentials")).append(".");
                        this.prompt(host.getProtocol(), credentials, title, reason.toString(),
                                enableKeychain, enablePublicKey, enableAnonymous);
                    }
                    // We decide later if the key is encrypted and a password must be known to decrypt.
                }
            }
            else {
                reason.append(Locale.localizedString(
                        "No login credentials could be found in the Keychain", "Credentials")).append(".");
                this.prompt(host.getProtocol(), credentials, title, reason.toString(),
                        enableKeychain, enablePublicKey, enableAnonymous);
            }
        }
    }

    @Override
    public void fail(final Protocol protocol, final Credentials credentials, final String reason)
            throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login failure as %s", credentials.getUsername()));
        }
        this.prompt(protocol, credentials,
                Locale.localizedString("Login failed", "Credentials"), reason);
    }

    @Override
    public void success(final Host host) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login success to host %s as %s", host.getHostname(), host.getCredentials().getUsername()));
        }
        KeychainFactory.get().save(host);
        if(BookmarkCollection.defaultCollection().contains(host)) {
            BookmarkCollection.defaultCollection().collectionItemChanged(host);
        }
    }

    @Override
    public void fail(final Protocol protocol, final Credentials credentials)
            throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login failure as %s", credentials.getUsername()));
        }
        this.prompt(protocol, credentials,
                Locale.localizedString("Login failed", "Credentials"),
                Locale.localizedString("Login with username and password", "Credentials"));
    }

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials,
                       final String title, final String reason) throws LoginCanceledException {
        this.prompt(protocol, credentials, title, reason, Preferences.instance().getBoolean("connection.login.useKeychain"),
                protocol.equals(Protocol.SFTP), protocol.isAnonymousConfigurable());
    }

    @Override
    public abstract void prompt(final Protocol protocol, final Credentials credentials,
                                final String title, final String reason,
                                final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous)
            throws LoginCanceledException;
}
