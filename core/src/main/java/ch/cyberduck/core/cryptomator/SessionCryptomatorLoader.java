package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.ContentReader;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;

import java.security.SecureRandom;
import java.util.EnumSet;

public class SessionCryptomatorLoader {
    private static final Logger log = Logger.getLogger(SessionCryptomatorLoader.class);

    private static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";
    private static final String BACKUPKEY_FILE_NAME = "masterkey.cryptomator.bkup";

    private final Session<?> session;

    private Cryptor cryptor;
    private LongFileNameProvider longFileNameProvider;
    private DirectoryIdProvider directoryIdProvider;
    private CryptoPathMapper cryptoPathMapper;

    public SessionCryptomatorLoader(final Session session) {
        this.session = session;
    }

    /**
     * @param home     Default path
     * @param callback Callback
     * @throws ch.cyberduck.core.exception.LoginCanceledException User dismissed passphrase prompt
     * @throws BackgroundException                                Failure reading master key from server
     */
    public void load(final Path home, final LoginCallback callback) throws BackgroundException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(new SecureRandom());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        final Path file = new Path(home, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt to read master key from %s", file));
        }
        final String masterKey = new ContentReader(session).readToString(file);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Read master key %s", masterKey));
        }
        final KeyFile keyFile = KeyFile.parse(masterKey.getBytes());
        final Credentials credentials = new Credentials();
        // Default to false for save in keychain
        credentials.setSaved(false);
        callback.prompt(session.getHost(), credentials,
                LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault", "Cryptomator"),
                new LoginOptions().user(false).anonymous(false));
        cryptor = provider.createFromKeyFile(keyFile, credentials.getPassword(), 5);
        longFileNameProvider = new LongFileNameProvider(home, session);
        directoryIdProvider = new DirectoryIdProvider(session);
        cryptoPathMapper = new CryptoPathMapper(home, cryptor, longFileNameProvider, directoryIdProvider);
    }

    public Cryptor getCryptor() {
        return cryptor;
    }

    public LongFileNameProvider getLongFileNameProvider() {
        return longFileNameProvider;
    }

    public DirectoryIdProvider getDirectoryIdProvider() {
        return directoryIdProvider;
    }

    public CryptoPathMapper getCryptoPathMapper() {
        return cryptoPathMapper;
    }
}
