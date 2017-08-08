package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.log4j.Logger;

import eu.ssp_europe.sds.crypto.Crypto;
import eu.ssp_europe.sds.crypto.CryptoException;
import eu.ssp_europe.sds.crypto.model.UserKeyPair;

public class TripleCryptKeyPair {
    private static final Logger log = Logger.getLogger(TripleCryptKeyPair.class);

    private final HostPasswordStore keychain = PasswordStoreFactory.get();

    public Credentials unlock(final PasswordCallback callback, final Host bookmark, final UserKeyPair keypair) throws CryptoException, LoginCanceledException {
        final String passphrase = keychain.getPassword(bookmark.getHostname(),
                String.format("Triple-Crypt Encryption Password (%s)", bookmark.getCredentials().getUsername()));
        return this.unlock(callback, bookmark, keypair, passphrase, LocaleFactory.localizedString("Enter your encryption password", "Credentials"));
    }

    private Credentials unlock(final PasswordCallback callback, final Host bookmark, final UserKeyPair keypair, String passphrase, final String message) throws LoginCanceledException, CryptoException {
        final Credentials credentials;
        if(null == passphrase) {
            credentials = callback.prompt(LocaleFactory.localizedString("Private key password protected", "Credentials"), message,
                    new LoginOptions()
                            .user(false)
                            .anonymous(false)
                            .icon(bookmark.getProtocol().disk())
            );
            if(credentials.getPassword() == null) {
                throw new LoginCanceledException();
            }
        }
        else {
            credentials = new VaultCredentials(passphrase);
            credentials.setSaved(true);
        }
        if(!Crypto.checkUserKeyPair(keypair, credentials.getPassword())) {
            return this.unlock(callback, bookmark, keypair, null, String.format("%s. %s", LocaleFactory.localizedString("Invalid passphrase", "Credentials"), LocaleFactory.localizedString("Enter your encryption password", "Credentials")));
        }
        else {
            if(credentials.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save encryption password for %s", bookmark));
                }
                keychain.addPassword(bookmark.getHostname(),
                        String.format("Triple-Crypt Encryption Password (%s)", bookmark.getCredentials().getUsername()),
                        passphrase);
            }
            return credentials;
        }
    }
}
