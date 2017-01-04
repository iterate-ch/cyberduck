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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryIdProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameProvider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultException;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonParseException;

/**
 * Cryptomator vault implementation
 */
public class CryptoVault implements Vault {
    private static final Logger log = Logger.getLogger(CryptoVault.class);

    public static final String DIR_PREFIX = "0";

    static {
        final int position = PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
    }

    public static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";
    public static final String BACKUPKEY_FILE_NAME = "masterkey.cryptomator.bkup";
    private static final Integer VAULT_VERSION = 5;

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    /**
     * Root of vault directory
     */
    private final Path home;
    private final PasswordStore keychain;

    private Cryptor cryptor;

    private final CryptoFilenameProvider filenameProvider;
    private final CryptoDirectoryIdProvider directoryIdProvider;
    private final CryptoDirectoryProvider directoryProvider;

    public CryptoVault(final Path home, final PasswordStore keychain) {
        this.home = home;
        this.keychain = keychain;
        this.filenameProvider = new CryptoFilenameProvider(home);
        this.directoryIdProvider = new CryptoDirectoryIdProvider();
        this.directoryProvider = new CryptoDirectoryProvider(home, this);
    }

    @Override
    public synchronized CryptoVault create(final Session<?> session, final String region, final PasswordCallback prompt) throws BackgroundException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(
                FastSecureRandomProvider.get().provide()
        );
        final Path file = new Path(home, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault));
        final Host bookmark = session.getHost();
        final Credentials credentials = new Credentials();
        prompt.prompt(credentials,
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Provide a passphrase for the Cryptomator Vault “{0}“", "Cryptomator"), home.getName()),
                new LoginOptions().user(false).anonymous(false).icon("cryptomator.tiff"));
        if(credentials.isSaved()) {
            keychain.addPassword(bookmark.getHostname(), file.getAbsolute(), credentials.getPassword());
        }
        final String passphrase = credentials.getPassword();
        final KeyFile master = provider.createNew().writeKeysToMasterkeyFile(passphrase, VAULT_VERSION);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write master key to %s", file));
        }
        final ContentWriter writer = new ContentWriter(session);
        // Obtain non encrypted directory writer
        final Directory feature = session._getFeature(Directory.class);
        feature.mkdir(home, region, new TransferStatus());
        writer.write(file, master.serialize());
        this.open(KeyFile.parse(master.serialize()), passphrase);
        final Path secondLevel = directoryProvider.toEncrypted(session, home).path;
        final Path firstLevel = secondLevel.getParent();
        final Path dataDir = firstLevel.getParent();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create vault root directory at %s", secondLevel));
        }
        feature.mkdir(dataDir, region, new TransferStatus());
        feature.mkdir(firstLevel, region, new TransferStatus());
        feature.mkdir(secondLevel, region, new TransferStatus());
        return this;
    }

    @Override
    public synchronized CryptoVault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        if(this.isUnlocked()) {
            log.warn(String.format("Skip unlock of open vault %s", this));
            return this;
        }
        final Path key = new Path(home, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt to read master key from %s", key));
        }
        final String json = new ContentReader(session).readToString(key);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Read master key %s", json));
        }
        final KeyFile master;
        try {
            master = KeyFile.parse(json.getBytes());
        }
        catch(JsonParseException | IllegalArgumentException | IllegalStateException e) {
            throw new VaultException(String.format("Failure reading vault master key file %s", key.getName()), e);
        }
        final Host bookmark = session.getHost();
        final Credentials credentials = new Credentials(bookmark.getHostname(),
                keychain.getPassword(bookmark.getHostname(), key.getAbsolute())) {
            @Override
            public String getPasswordPlaceholder() {
                return LocaleFactory.localizedString("Passphrase", "Cryptomator");
            }
        };
        this.unlock(key, master, credentials, prompt,
                MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault “{0}“", "Cryptomator"), home.getName()));
        return this;
    }

    private void unlock(final Path key, final KeyFile master, final Credentials credentials, final PasswordCallback prompt, final String message) throws LoginCanceledException, VaultException {
        if(null == credentials.getPassword()) {
            prompt.prompt(credentials,
                    LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                    message,
                    new LoginOptions().user(false).anonymous(false).icon("cryptomator.tiff"));
            if(null == credentials.getPassword()) {
                throw new LoginCanceledException();
            }
        }
        try {
            this.open(master, credentials.getPassword());
            if(credentials.isSaved()) {
                keychain.addPassword(credentials.getUsername(), key.getAbsolute(), credentials.getPassword());
            }
        }
        catch(CryptoAuthenticationException e) {
            credentials.setPassword(null);
            this.unlock(key, master, credentials,
                    prompt, String.format("%s %s.", e.getDetail(),
                            MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault “{0}“", "Cryptomator"), home.getName())));
        }
        finally {
            credentials.setPassword(null);
        }
    }

    @Override
    public synchronized void close() {
        if(this.isUnlocked()) {
            if(cryptor != null) {
                cryptor.destroy();
            }
            filenameProvider.close();
            directoryIdProvider.close();
            directoryProvider.close();
        }
        cryptor = null;
    }

    private void open(final KeyFile keyFile, final CharSequence passphrase) throws VaultException, CryptoAuthenticationException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(
                FastSecureRandomProvider.get().provide()
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        try {
            cryptor = provider.createFromKeyFile(keyFile, passphrase, VAULT_VERSION);
        }
        catch(IllegalArgumentException e) {
            throw new VaultException("Failure reading key file", e);
        }
        catch(InvalidPassphraseException e) {
            throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
        }
    }

    public synchronized boolean isUnlocked() {
        return cryptor != null;
    }

    @Override
    public State getState() {
        return this.isUnlocked() ? State.open : State.closed;
    }

    @Override
    public boolean contains(final Path file) {
        if(file.getType().contains(Path.Type.vault)) {
            return false;
        }
        if(this.isUnlocked()) {
            return file.equals(home) || file.isChild(home);
        }
        return false;
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return this.encrypt(session, file, false);
    }

    public Path encrypt(final Session<?> session, final Path file, boolean metadata) throws BackgroundException {
        if(this.contains(file)) {
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn(String.format("Skip file %s because it is already marked as an ecrypted path", file));
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                log.warn(String.format("Skip file %s because it is marked as an internal vault path", file));
                return file;
            }
            if(file.isFile() || metadata) {
                final CryptoDirectory parent = directoryProvider.toEncrypted(session, file.getParent());
                final String filename = directoryProvider.toEncrypted(session, parent.id, file.getName(), file.getType());
                return new Path(parent.path, filename, EnumSet.of(Path.Type.file, Path.Type.encrypted), file.attributes());
            }
            else {
                final CryptoDirectory cryptoDirectory = directoryProvider.toEncrypted(session, file);
                // Set internal id
                cryptoDirectory.path.attributes().setDirectoryId(cryptoDirectory.id);
                return cryptoDirectory.path;
            }
        }
        return file;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path directory, final Path file) throws BackgroundException {
        if(this.contains(directory)) {
            if(file.getType().contains(Path.Type.decrypted)) {
                log.warn(String.format("Skip file %s because it is already marked as an decrypted path", file));
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                log.warn(String.format("Skip file %s because it is marked as an internal vault path", file));
                return file;
            }
            final Path inflated = this.inflate(session, file);
            final Matcher m = BASE32_PATTERN.matcher(inflated.getName());
            if(m.find()) {
                final String ciphertext = m.group(1);
                try {
                    final CryptoDirectory cryptoDirectory = directoryProvider.toEncrypted(session, directory);
                    final String cleartextFilename = cryptor.fileNameCryptor().decryptFilename(
                            ciphertext, cryptoDirectory.id.getBytes(StandardCharsets.UTF_8));
                    final Path decrypted = new Path(directory, cleartextFilename,
                            EnumSet.of(inflated.getName().startsWith(DIR_PREFIX) ? Path.Type.directory : Path.Type.file, Path.Type.decrypted),
                            file.attributes());
                    if(decrypted.isDirectory()) {
                        final Permission permission = decrypted.attributes().getPermission();
                        permission.setUser(permission.getUser().or(Permission.Action.execute));
                        permission.setGroup(permission.getGroup().or(Permission.Action.execute));
                        permission.setOther(permission.getOther().or(Permission.Action.execute));
                    }
                    else {
                        decrypted.attributes().setSize(this.toCleartextSize(file.attributes().getSize()));
                    }
                    return decrypted;
                }
                catch(AuthenticationFailedException e) {
                    throw new CryptoAuthenticationException(
                            "Failure to decrypt due to an unauthentic ciphertext", e);
                }
            }
            else {
                throw new CryptoFilenameMismatchException(
                        String.format("Failure to decrypt due to missing pattern match for %s", BASE32_PATTERN));
            }
        }
        return file;
    }

    @Override
    public long toCiphertextSize(final long cleartextFileSize) {
        if(-1L == cleartextFileSize) {
            return -1L;
        }
        final int headerSize = cryptor.fileHeaderCryptor().headerSize();
        final int cleartextChunkSize = cryptor.fileContentCryptor().cleartextChunkSize();
        final int chunkHeaderSize = cryptor.fileContentCryptor().ciphertextChunkSize() - cleartextChunkSize;
        return cleartextFileSize + headerSize + (cleartextFileSize > 0 ? chunkHeaderSize : 0) + chunkHeaderSize * ((cleartextFileSize - 1) / cleartextChunkSize);
    }

    @Override
    public long toCleartextSize(final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
        if(-1L == ciphertextFileSize) {
            return -1L;
        }
        final int headerSize = cryptor.fileHeaderCryptor().headerSize();
        final int ciphertextChunkSize = cryptor.fileContentCryptor().ciphertextChunkSize();
        final int chunkHeaderSize = ciphertextChunkSize - cryptor.fileContentCryptor().cleartextChunkSize();
        if(ciphertextFileSize < headerSize) {
            throw new CryptoInvalidFilesizeException(String.format("Encrypted file size must be at least %d bytes", headerSize));
        }
        final long remainder = (ciphertextFileSize - headerSize) % ciphertextChunkSize;
        if(remainder > 0 && remainder < chunkHeaderSize) {
            throw new CryptoInvalidFilesizeException("Invalid file size");
        }
        return ciphertextFileSize - (headerSize + (ciphertextFileSize / ciphertextChunkSize) * chunkHeaderSize + (remainder == 0 ? 0 : chunkHeaderSize));
    }

    private Path inflate(final Session<?> session, final Path file) throws BackgroundException {
        final String fileName = file.getName();
        if(filenameProvider.isDeflated(fileName)) {
            final String filename = filenameProvider.inflate(session, fileName);
            return new Path(file.getParent(), filename, file.getType(), file.attributes());
        }
        else {
            return file;
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

    public CryptoDirectoryProvider getDirectoryProvider() {
        return directoryProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) {
        if(this.isUnlocked()) {
            if(type == ListService.class) {
                return (T) new CryptoListService(session, (ListService) delegate, this);
            }
            if(type == Touch.class) {
                return (T) new CryptoTouchFeature(session, new DefaultTouchFeature(session), this);
            }
            if(type == Directory.class) {
                return (T) new CryptoDirectoryFeature(session, (Directory) delegate, this);
            }
            if(type == Upload.class) {
                return (T) new CryptoUploadFeature(session, (Upload) delegate, this);
            }
            if(type == Download.class) {
                return (T) new CryptoDownloadFeature(session, (Download) delegate, this);
            }
            if(type == Read.class) {
                return (T) new CryptoReadFeature(session, (Read) delegate, this);
            }
            if(type == Write.class) {
                return (T) new CryptoWriteFeature(session, (Write) delegate, this);
            }
            if(type == Move.class) {
                return (T) new CryptoMoveFeature(session, (Move) delegate, this);
            }
            if(type == AttributesFinder.class) {
                return (T) new CryptoAttributesFeature(session, (AttributesFinder) delegate, this);
            }
            if(type == Find.class) {
                return (T) new CryptoFindFeature(session, (Find) delegate, this);
            }
            if(type == UrlProvider.class) {
                return (T) new CryptoUrlProvider(session, (UrlProvider) delegate, this);
            }
            if(type == IdProvider.class) {
                return (T) new CryptoIdProvider(session, (IdProvider) delegate, this);
            }
            if(type == Delete.class) {
                return (T) new CryptoDeleteFeature(session, (Delete) delegate, this);
            }
            if(type == Symlink.class) {
                return (T) new CryptoSymlinkFeature(session, (Symlink) delegate, this);
            }
            if(type == Headers.class) {
                return (T) new CryptoHeadersFeature(session, (Headers) delegate, this);
            }
            if(type == Compress.class) {
                return (T) new CryptoCompressFeature(session, (Compress) delegate, this);
            }
            if(type == Bulk.class) {
                return (T) new CryptoBulkFeature<>(session, (Bulk) delegate, this);
            }
            if(type == ChecksumCompute.class) {
                return (T) new CryptoChecksumCompute((ChecksumCompute) delegate, this);
            }
            if(type == UnixPermission.class) {
                return (T) new CryptoUnixPermission(session, (UnixPermission) delegate, this);
            }
            if(type == AclPermission.class) {
                return (T) new CryptoAclPermission(session, (AclPermission) delegate, this);
            }
        }
        return delegate;
    }

    public static final class CryptoDirectory {
        public final String id;
        public final Path path;

        public CryptoDirectory(final String id, final Path path) {
            this.id = id;
            this.path = path;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof CryptoVault)) {
            return false;
        }
        final CryptoVault that = (CryptoVault) o;
        return Objects.equals(home, that.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(home);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoVault{");
        sb.append("home=").append(home);
        sb.append(", cryptor=").append(cryptor);
        sb.append('}');
        return sb.toString();
    }
}
