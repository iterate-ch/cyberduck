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

    public void unlock(final PasswordCallback callback, final Host bookmark, final VaultCredentials passphrase, final UserKeyPair keypair) throws CryptoException, LoginCanceledException {
        if(null == passphrase.getPassword()) {
            callback.prompt(passphrase,
                    LocaleFactory.localizedString("Private key password protected", "Credentials"),
                    LocaleFactory.localizedString("Enter your encryption password", "Credentials"),
                    new LoginOptions()
                            .user(false)
                            .anonymous(false)
                            .icon(bookmark.getProtocol().disk())
            );
            if(passphrase.getPassword() == null) {
                throw new LoginCanceledException();
            }
        }
        if(!Crypto.checkUserKeyPair(keypair, passphrase.getPassword())) {
            passphrase.setPassword(null);
            this.unlock(callback, bookmark, passphrase, keypair);
        }
        else {
            if(passphrase.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save encryption password for %s", bookmark));
                }
                keychain.addPassword(bookmark.getHostname(),
                        String.format("Triple-Crypt Encryption Password (%s)", bookmark.getCredentials().getUsername()),
                        passphrase.getPassword());
            }
        }
    }
}
