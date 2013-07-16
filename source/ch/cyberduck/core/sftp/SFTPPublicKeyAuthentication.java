package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.local.Local;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.putty.PuTTYKey;

import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMDecryptException;

/**
 * @version $Id:$
 */
public class SFTPPublicKeyAuthentication {
    private static final Logger log = Logger.getLogger(SFTPPublicKeyAuthentication.class);

    private SFTPSession session;

    public SFTPPublicKeyAuthentication(final SFTPSession session) {
        this.session = session;
    }

    public boolean authenticate(final Host host, final LoginController prompt)
            throws IOException, LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using public key authentication with credentials %s", host.getCredentials()));
        }
        if(session.getClient().isAuthMethodAvailable(host.getCredentials().getUsername(), "publickey")) {
            if(host.getCredentials().isPublicKeyAuthentication()) {
                final Local identity = host.getCredentials().getIdentity();
                final CharArrayWriter privatekey = new CharArrayWriter();
                if(PuTTYKey.isPuTTYKeyFile(identity.getInputStream())) {
                    final PuTTYKey putty = new PuTTYKey(identity.getInputStream());
                    if(putty.isEncrypted()) {
                        if(StringUtils.isEmpty(host.getCredentials().getPassword())) {
                            prompt.prompt(host.getProtocol(), host.getCredentials(),
                                    Locale.localizedString("Private key password protected", "Credentials"),
                                    Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                            + " (" + identity + ")",
                                    new LoginOptions(host.getProtocol()));
                        }
                    }
                    try {
                        IOUtils.copy(new StringReader(putty.toOpenSSH(host.getCredentials().getPassword())), privatekey);
                    }
                    catch(PEMDecryptException e) {
                        prompt.prompt(host.getProtocol(), host.getCredentials(),
                                Locale.localizedString("Invalid passphrase", "Credentials"),
                                Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                        + " (" + identity + ")", new LoginOptions(host.getProtocol()));
                        return this.authenticate(host, prompt);
                    }
                }
                else {
                    IOUtils.copy(new FileReader(identity.getAbsolute()), privatekey);
                    if(PEMDecoder.isPEMEncrypted(privatekey.toCharArray())) {
                        if(StringUtils.isEmpty(host.getCredentials().getPassword())) {
                            prompt.prompt(host.getProtocol(), host.getCredentials(),
                                    Locale.localizedString("Private key password protected", "Credentials"),
                                    Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                            + " (" + identity + ")", new LoginOptions(host.getProtocol()));
                        }
                    }
                    try {
                        PEMDecoder.decode(privatekey.toCharArray(), host.getCredentials().getPassword());
                    }
                    catch(PEMDecryptException e) {
                        prompt.prompt(host.getProtocol(), host.getCredentials(),
                                Locale.localizedString("Invalid passphrase", "Credentials"),
                                Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                        + " (" + identity + ")", new LoginOptions(host.getProtocol()));
                        return this.authenticate(host, prompt);
                    }
                }
                return session.getClient().authenticateWithPublicKey(host.getCredentials().getUsername(),
                        privatekey.toCharArray(), host.getCredentials().getPassword());
            }
        }
        return false;

    }
}
