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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.model.PlainDataContainer;
import com.dracoon.sdk.crypto.model.UserKeyPair;

public class TripleCryptKeyPair {
    private static final Logger log = LogManager.getLogger(TripleCryptKeyPair.class);

    private final HostPasswordStore keychain = PasswordStoreFactory.get();

    public Credentials unlock(final PasswordCallback callback, final Host bookmark, final UserKeyPair keypair) throws CryptoException, BackgroundException {
        final String passphrase = keychain.getPassword(toServiceName(bookmark, keypair.getUserPublicKey().getVersion()), toAccountName(bookmark));
        return this.unlock(callback, bookmark, keypair, passphrase);
    }

    public Credentials unlock(final PasswordCallback callback, final Host bookmark, final UserKeyPair keypair, final String passphrase) throws CryptoException, LoginCanceledException {
        return this.unlock(callback, bookmark, keypair, passphrase, LocaleFactory.localizedString("Enter your decryption password to access encrypted data rooms.", "SDS"));
    }

    private Credentials unlock(final PasswordCallback callback, final Host bookmark, final UserKeyPair keypair, String passphrase, final String message) throws LoginCanceledException, CryptoException {
        final Credentials credentials;
        if(null == passphrase) {
            credentials = callback.prompt(bookmark, LocaleFactory.localizedString("Decryption password required", "SDS"), message,
                    new LoginOptions()
                            .icon(bookmark.getProtocol().disk())
            );
            if(credentials.getPassword() == null) {
                throw new LoginCanceledException();
            }
        }
        else {
            credentials = new VaultCredentials(passphrase).withSaved(false);
        }
        if(!Crypto.checkUserKeyPair(keypair, credentials.getPassword())) {
            return this.unlock(callback, bookmark, keypair, null, String.format("%s. %s", LocaleFactory.localizedString("Invalid passphrase", "Credentials"), LocaleFactory.localizedString("Enter your decryption password to access encrypted data rooms.", "SDS")));
        }
        else {
            if(credentials.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save encryption password for %s", bookmark));
                }
                try {
                    keychain.addPassword(toServiceName(bookmark, keypair.getUserPublicKey().getVersion()),
                            toAccountName(bookmark), credentials.getPassword());
                }
                catch(LocalAccessDeniedException e) {
                    log.error(String.format("Failure %s saving credentials for %s in password store", e, bookmark));
                }
            }
            return credentials;
        }
    }

    protected static String toServiceName(final Host bookmark, final UserKeyPair.Version version) {
        return String.format("Triple-Crypt Encryption Password (%s) - Version (%s)", bookmark.getCredentials().getUsername(), version.getValue());
    }

    protected static String toAccountName(final Host bookmark) {
        return new DefaultUrlProvider(bookmark).toUrl(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory))).find(DescriptiveUrl.Type.provider).getUrl();
    }

    public static PlainDataContainer createPlainDataContainer(final byte[] bytes, final int len) {
        final byte[] b = new byte[len];
        System.arraycopy(bytes, 0, b, 0, len);
        return new PlainDataContainer(b);
    }
}
