package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.CryptoAuthenticationException;
import ch.cyberduck.core.cryptomator.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.CryptoListService;
import ch.cyberduck.core.cryptomator.CryptoMoveFeature;
import ch.cyberduck.core.cryptomator.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.VaultFinder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ContentReader;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.EnumSet;

/**
 * Cryptomator vault implementation
 */
public class CryptoVault implements Vault {
    private static final Logger log = Logger.getLogger(CryptoVault.class);

    static {
        final int position = PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
    }

    private static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";
    private static final String BACKUPKEY_FILE_NAME = "masterkey.cryptomator.bkup";

    private final Session<?> session;

    private Cryptor cryptor;
    private LongFileNameProvider longFileNameProvider;
    private DirectoryIdProvider directoryIdProvider;
    private CryptoPathMapper cryptoPathMapper;

    public CryptoVault(final Session<?> session) {
        this.session = session;
    }

    /**
     * Create vault
     *
     * @param home
     * @param keychain
     * @param callback
     * @throws BackgroundException
     */
    @Override
    public void create(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
        throw new NotfoundException(home.getAbsolute());
    }

    /**
     * Open vault
     *
     * @param home     Default path
     * @param callback Callback
     * @throws ch.cyberduck.core.exception.LoginCanceledException User dismissed passphrase prompt
     * @throws BackgroundException                                Failure reading master key from server
     * @throws NotfoundException                                  No master key file in home
     * @throws CryptoAuthenticationException                      Failure opening master key file
     */
    @Override
    public void load(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(new SecureRandom());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        final Path file = new Path(home, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file));
        if(!session.getFeature(Find.class).find(file)) {
            throw new NotfoundException(file.getAbsolute());
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt to read master key from %s", file));
            }
            final String masterKey = new ContentReader(session).readToString(file);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Read master key %s", masterKey));
            }
            final KeyFile keyFile = KeyFile.parse(masterKey.getBytes());
            final Host bookmark = session.getHost();
            String passphrase = keychain.getPassword(bookmark.getHostname(), file.getAbsolute());
            if(null == passphrase) {
                final Credentials credentials = new Credentials();
                // Default to false for save in keychain
                credentials.setSaved(false);
                callback.prompt(bookmark, credentials,
                        LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                        LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault", "Cryptomator"),
                        new LoginOptions().user(false).anonymous(false));
                if(credentials.isSaved()) {
                    keychain.addPassword(bookmark.getHostname(), file.getAbsolute(), credentials.getPassword());
                }
                passphrase = credentials.getPassword();
            }
            try {
                cryptor = provider.createFromKeyFile(keyFile, passphrase, 5);
            }
            catch(InvalidPassphraseException e) {
                throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
            }
            longFileNameProvider = new LongFileNameProvider(home, session);
            directoryIdProvider = new DirectoryIdProvider(session);
            cryptoPathMapper = new CryptoPathMapper(home, this);
        }
    }

    @Override
    public boolean isLoaded() {
        return cryptor != null;
    }

    public Path encrypt(final Path file) throws BackgroundException {
        try {
            final CryptoPathMapper.Directory ciphertextDirectory = cryptoPathMapper.getCiphertextDir(file.getParent());
            final String ciphertextFileName = cryptoPathMapper.getCiphertextFileName(ciphertextDirectory.dirId, file.getName(), EnumSet.of(Path.Type.file));
            return new Path(ciphertextDirectory.path, ciphertextFileName, EnumSet.of(Path.Type.file));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final T delegate) {
        if(this.isLoaded()) {
            if(type == Home.class) {
                return (T) new VaultFinder(this, (Home) delegate, null, null);
            }
            if(type == ListService.class) {
                return (T) new CryptoListService((ListService) delegate, this);
            }
            if(type == Touch.class) {
                return (T) new CryptoTouchFeature((Touch) delegate, this);
            }
            if(type == Directory.class) {
                return (T) new CryptoDirectoryFeature((Directory) delegate, this);
            }
            if(type == Read.class) {
                return (T) new CryptoReadFeature((Read) delegate, this);
            }
            if(type == Write.class) {
                return (T) new CryptoWriteFeature((Write) delegate, this);
            }
            if(type == Move.class) {
                return (T) new CryptoMoveFeature((Move) delegate, this);
            }
            if(type == Attributes.class) {
                return (T) new CryptoAttributesFeature((Attributes) delegate, this);
            }
            if(type == Find.class) {
                return (T) new CryptoFindFeature((Find) delegate, this);
            }
        }
        return delegate;
    }
}
