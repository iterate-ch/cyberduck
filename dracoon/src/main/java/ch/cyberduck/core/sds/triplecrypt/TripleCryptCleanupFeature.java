package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Pairing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dracoon.sdk.crypto.model.UserKeyPair;

public class TripleCryptCleanupFeature implements Pairing {
    private static final Logger log = LogManager.getLogger(TripleCryptCleanupFeature.class);

    private final HostPasswordStore keychain = PasswordStoreFactory.get();

    @Override
    public void delete(final Host bookmark) throws BackgroundException {
        {
            final String account = TripleCryptKeyPair.toServiceName(bookmark, UserKeyPair.Version.RSA2048);
            if(log.isDebugEnabled()) {
                log.debug("Delete credentials for {} in keychain {}", account, keychain);
            }
            keychain.deletePassword(account, TripleCryptKeyPair.toAccountName(bookmark));
        }
        {
            final String account = TripleCryptKeyPair.toServiceName(bookmark, UserKeyPair.Version.RSA4096);
            if(log.isDebugEnabled()) {
                log.debug("Delete credentials for {} in keychain {}", account, keychain);
            }
            keychain.deletePassword(account, TripleCryptKeyPair.toAccountName(bookmark));
        }
    }
}
