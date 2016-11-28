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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.common.SecureRandomModule;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonParseException;

/**
 * Cryptomator vault implementation
 */
public class CryptoVault implements Vault {
    private static final Logger log = Logger.getLogger(CryptoVault.class);

    private static final SecureRandom random;

    static {
        final int position = PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
    }

    static {
        try {
            final SecureRandom seeder = SecureRandom.getInstanceStrong();
            random = new SecureRandomModule(seeder).provideFastSecureRandom(seeder);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA1PRNG must exist in every Java platform.", e);
        }
    }

    public static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";
    public static final String BACKUPKEY_FILE_NAME = "masterkey.cryptomator.bkup";
    private static final Integer VAULT_VERSION = 5;

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    private final Session<?> session;

    private Cryptor cryptor;
    private CryptoFilenameProvider filenameProvider;
    private CryptoDirectoryIdProvider directoryIdProvider;
    private CryptoDirectoryProvider cryptoDirectoryProvider;

    public CryptoVault(final Session<?> session) {
        this.session = session;
    }

    /**
     * Create and open vault
     *
     * @param home     Target for vault
     * @param keychain Password store
     * @param callback Password prompt
     */
    @Override
    public void create(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(random);
        final Path file = new Path(home, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file));
        final Host bookmark = session.getHost();
        final Credentials credentials = new Credentials();
        // Default to false for save in keychain
        credentials.setSaved(false);
        callback.prompt(bookmark, credentials,
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                LocaleFactory.localizedString("Provide a passphrase for the Cryptomator Vault", "Cryptomator"),
                new LoginOptions().user(false).anonymous(false));
        if(credentials.isSaved()) {
            keychain.addPassword(bookmark.getHostname(), file.getAbsolute(), credentials.getPassword());
        }
        final String passphrase = credentials.getPassword();
        final KeyFile keyFile = provider.createNew().writeKeysToMasterkeyFile(passphrase, VAULT_VERSION);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write master key to %s", file));
        }
        final ContentWriter writer = new ContentWriter(session);
        writer.write(file, keyFile.serialize());
        this.init(home, keyFile, passphrase);
        try {
            final Path secondLevel = cryptoDirectoryProvider.toEncrypted(home).path;
            final Path firstLevel = secondLevel.getParent();
            final Path dataDir = firstLevel.getParent();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create vault root directory at %s", secondLevel));
            }
            final Directory feature = session._getFeature(Directory.class);
            feature.mkdir(dataDir);
            feature.mkdir(firstLevel);
            feature.mkdir(secondLevel);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    /**
     * Open vault
     *
     * @param home     Default path
     * @param callback Callback
     * @throws VaultException                Failure parsing master key
     * @throws LoginCanceledException        User dismissed passphrase prompt
     * @throws BackgroundException           Failure reading master key from server
     * @throws NotfoundException             No master key file in home
     * @throws CryptoAuthenticationException Failure opening master key file
     */
    @Override
    public void load(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
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
            final KeyFile masterKeyFile;
            try {
                masterKeyFile = KeyFile.parse(masterKey.getBytes());
            }
            catch(JsonParseException | IllegalArgumentException | IllegalStateException e) {
                throw new VaultException(String.format("Failure reading vault master key file %s", file.getName()), e);
            }
            final Host bookmark = session.getHost();
            String passphrase = keychain.getPassword(bookmark.getHostname(), file.getAbsolute());
            if(null == passphrase) {
                final Credentials credentials = new Credentials() {
                    @Override
                    public String getPasswordPlaceholder() {
                        return LocaleFactory.localizedString("Passphrase", "Cryptomator");
                    }
                };
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
            this.init(home, masterKeyFile, passphrase);
        }
    }

    private void init(final Path home, final KeyFile keyFile, final CharSequence passphrase) throws CryptoAuthenticationException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(random);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        try {
            cryptor = provider.createFromKeyFile(keyFile, passphrase, VAULT_VERSION);
        }
        catch(InvalidPassphraseException e) {
            throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
        }
        this.filenameProvider = new CryptoFilenameProvider(home, session);
        this.directoryIdProvider = new CryptoDirectoryIdProvider(session);
        this.cryptoDirectoryProvider = new CryptoDirectoryProvider(home, this);
    }

    @Override
    public boolean isLoaded() {
        return cryptor != null;
    }

    @Override
    public Path encrypt(final Path file) throws BackgroundException {
        try {
            if(file.isDirectory()) {
                final CryptoDirectory directory = cryptoDirectoryProvider.toEncrypted(file);
                return directory.path;
            }
            else {
                final CryptoDirectory parent = cryptoDirectoryProvider.toEncrypted(file.getParent());
                final String filename = cryptoDirectoryProvider.toEncrypted(parent.id, file.getName(), EnumSet.of(Path.Type.file));
                return new Path(parent.path, filename, EnumSet.of(Path.Type.file), file.attributes());
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private Path inflate(final Path file) throws CryptoAuthenticationException {
        final String fileName = file.getName();
        if(filenameProvider.isDeflated(fileName)) {
            try {
                final String filename = filenameProvider.inflate(fileName);
                return new Path(file.getParent(), filename, file.getType(), file.attributes());
            }
            catch(IOException e) {
                throw new CryptoAuthenticationException(
                        String.format("Failure to inflate filename from %s", file.getName()), e);
            }
        }
        else {
            return file;
        }
    }

    @Override
    public Path decrypt(final Path directory, final Path file) throws BackgroundException {
        try {
            final Path inflated = this.inflate(file);
            final Matcher m = BASE32_PATTERN.matcher(inflated.getName());
            final CryptoDirectory cryptoDirectory = cryptoDirectoryProvider.toEncrypted(directory);
            if(m.find()) {
                final String ciphertext = m.group(1);
                try {
                    final String cleartextFilename = cryptor.fileNameCryptor().decryptFilename(
                            ciphertext, cryptoDirectory.id.getBytes(StandardCharsets.UTF_8));
                    final Path decrypted = new Path(directory, cleartextFilename,
                            inflated.getName().startsWith(Constants.DIR_PREFIX) ?
                                    EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), file.attributes());
                    if(decrypted.isDirectory()) {
                        final Permission permission = decrypted.attributes().getPermission();
                        permission.setUser(permission.getUser().or(Permission.Action.execute));
                        permission.setGroup(permission.getGroup().or(Permission.Action.execute));
                        permission.setOther(permission.getOther().or(Permission.Action.execute));
                    }
                    return decrypted;
                }
                catch(AuthenticationFailedException e) {
                    throw new CryptoAuthenticationException(
                            "Failure to decrypt due to an unauthentic ciphertext", e);
                }
            }
            else {
                throw new CryptoAuthenticationException(
                        String.format("Failure to decrypt due to missing pattern match for %s", BASE32_PATTERN));
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public Cryptor getCryptor() {
        return cryptor;
    }

    public CryptoFilenameProvider getFilenameProvider() {
        return filenameProvider;
    }

    public CryptoDirectoryIdProvider getDirectoryIdProvider() {
        return directoryIdProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final T delegate) {
        if(this.isLoaded()) {
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
            if(type == AttributesFinder.class) {
                return (T) new CryptoAttributesFeature((AttributesFinder) delegate, this);
            }
            if(type == Find.class) {
                return (T) new CryptoFindFeature((Find) delegate, this);
            }
            if(type == UrlProvider.class) {
                return (T) new CryptoUrlProvider((UrlProvider) delegate, this);
            }
            if(type == IdProvider.class) {
                return (T) new CryptoIdProvider((IdProvider) delegate, this);
            }
            if(type == Delete.class) {
                return (T) new CryptoDeleteFeature((Delete) delegate, this);
            }
            if(type == Symlink.class) {
                return (T) new CryptoSymlinkFeature((Symlink) delegate, this);
            }
            if(type == Compress.class) {
                return (T) new CryptoCompressFeature((Compress) delegate, this);
            }
        }
        return delegate;
    }
}
