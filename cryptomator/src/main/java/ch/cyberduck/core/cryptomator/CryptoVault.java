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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.features.*;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryProvider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameProvider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.Cryptors;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.cryptolib.api.KeyFile;
import org.cryptomator.cryptolib.v1.Version1CryptorModule;

import java.nio.charset.StandardCharsets;
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

    private static final int VAULT_VERSION = 6;

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    /**
     * Root of vault directory
     */
    private final Path home;
    private final Path masterkey;

    private final Preferences preferences = PreferencesFactory.get();

    private Cryptor cryptor;

    private final CryptoFilenameProvider filenameProvider;
    private final CryptoDirectoryProvider directoryProvider;

    private final byte[] pepper;

    public CryptoVault(final Path home) {
        this(home, DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME);
    }

    public CryptoVault(final Path home, final String masterkey) {
        this(home, masterkey, new byte[0]);
    }

    public CryptoVault(final Path home, final String masterkey, final byte[] pepper) {
        this.home = home;
        this.masterkey = new Path(home, masterkey, EnumSet.of(Path.Type.file, Path.Type.vault));
        this.pepper = pepper;
        // New vault home with vault flag set for internal use
        final EnumSet<Path.Type> type = EnumSet.copyOf(home.getType());
        type.add(Path.Type.vault);
        final Path vault = new Path(home.getAbsolute(), type, new PathAttributes(home.attributes()));
        this.filenameProvider = new CryptoFilenameProvider(vault);
        this.directoryProvider = new CryptoDirectoryProvider(vault, this);
    }

    @Override
    public synchronized Path create(final Session<?> session, final String region, final VaultCredentials credentials, final PasswordStore keychain) throws BackgroundException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(
            FastSecureRandomProvider.get().provide()
        );
        final Host bookmark = session.getHost();
        if(credentials.isSaved()) {
            try {
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                    new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
            }
            catch(LocalAccessDeniedException e) {
                log.error(String.format("Failure saving credentials for %s in keychain. %s", bookmark, e));
            }
        }
        final String passphrase = credentials.getPassword();
        final KeyFile masterKeyFileContent = provider.createNew().writeKeysToMasterkeyFile(passphrase, pepper, VAULT_VERSION);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write master key to %s", masterkey));
        }
        // Obtain non encrypted directory writer
        final Directory directory = session._getFeature(Directory.class);
        final TransferStatus status = new TransferStatus();
        final Encryption encryption = session.getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(home));
        }
        final Redundancy redundancy = session.getFeature(Redundancy.class);
        if(redundancy != null) {
            status.setStorageClass(redundancy.getDefault());
        }
        final Path vault = directory.mkdir(home, region, status);
        new ContentWriter(session).write(masterkey, masterKeyFileContent.serialize());
        this.open(KeyFile.parse(masterKeyFileContent.serialize()), passphrase);
        final Path secondLevel = directoryProvider.toEncrypted(session, home.attributes().getDirectoryId(), home);
        final Path firstLevel = secondLevel.getParent();
        final Path dataDir = firstLevel.getParent();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create vault root directory at %s", secondLevel));
        }
        directory.mkdir(dataDir, region, status);
        directory.mkdir(firstLevel, region, status);
        directory.mkdir(secondLevel, region, status);
        return vault;
    }

    @Override
    public synchronized CryptoVault load(final Session<?> session, final PasswordCallback prompt, final PasswordStore keychain) throws BackgroundException {
        if(this.isUnlocked()) {
            log.warn(String.format("Skip unlock of open vault %s", this));
            return this;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Attempt to read master key from %s", masterkey));
        }
        final String json = new ContentReader(session).read(masterkey);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Read master key %s", masterkey));
        }
        final KeyFile masterKeyFileContent;
        try {
            masterKeyFileContent = KeyFile.parse(json.getBytes());
        }
        catch(JsonParseException | IllegalArgumentException | IllegalStateException e) {
            throw new VaultException(String.format("Failure reading vault master key file %s", masterkey.getName()), e);
        }
        final Host bookmark = session.getHost();
        String passphrase = keychain.getPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
            new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl());
        if(null == passphrase) {
            // Legacy
            passphrase = keychain.getPassword(String.format("Cryptomator Passphrase %s", bookmark.getHostname()),
                new DefaultUrlProvider(bookmark).toUrl(masterkey).find(DescriptiveUrl.Type.provider).getUrl());
        }
        this.unlock(session, masterkey, masterKeyFileContent, passphrase, bookmark, prompt,
            MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault “{0}“", "Cryptomator"), home.getName()),
            keychain);
        return this;
    }

    private void unlock(final Session<?> session, final Path masterKeyFile, final KeyFile masterKeyFileContent,
                        final String passphrase, final Host bookmark, final PasswordCallback prompt, final String message, final PasswordStore keychain) throws BackgroundException {
        final Credentials credentials;
        if(null == passphrase) {
            credentials = prompt.prompt(
                bookmark, LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                message,
                new LoginOptions()
                    .user(false)
                    .anonymous(false)
                    .icon("cryptomator.tiff")
                    .passwordPlaceholder(LocaleFactory.localizedString("Passphrase", "Cryptomator")));
            if(null == credentials.getPassword()) {
                throw new LoginCanceledException();
            }
        }
        else {
            credentials = new VaultCredentials(passphrase).withSaved(preferences.getBoolean("vault.keychain"));
        }
        try {
            this.open(this.upgrade(session, masterKeyFileContent, credentials.getPassword()), credentials.getPassword());
            if(credentials.isSaved()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save passphrase for %s", masterKeyFile));
                }
                // Save password with hostname and path to masterkey.cryptomator in keychain
                keychain.addPassword(String.format("Cryptomator Passphrase (%s)", bookmark.getCredentials().getUsername()),
                    new DefaultUrlProvider(bookmark).toUrl(masterKeyFile).find(DescriptiveUrl.Type.provider).getUrl(), credentials.getPassword());
                // Save masterkey.cryptomator content in preferences
                preferences.setProperty(new DefaultUrlProvider(bookmark).toUrl(masterKeyFile).find(DescriptiveUrl.Type.provider).getUrl(),
                    new String(masterKeyFileContent.serialize()));
            }
        }
        catch(CryptoAuthenticationException e) {
            this.unlock(session, masterKeyFile, masterKeyFileContent, null, bookmark,
                prompt, String.format("%s %s.", e.getDetail(),
                    MessageFormat.format(LocaleFactory.localizedString("Provide your passphrase to unlock the Cryptomator Vault “{0}“", "Cryptomator"), home.getName())), keychain);
        }
    }

    @Override
    public synchronized void close() {
        if(this.isUnlocked()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Close vault with cryptor %s", cryptor));
            }
            if(cryptor != null) {
                cryptor.destroy();
            }
            if(directoryProvider != null) {
                directoryProvider.destroy();
            }
            if(filenameProvider != null) {
                filenameProvider.destroy();
            }
        }
        cryptor = null;
    }

    private KeyFile upgrade(final Session<?> session, final KeyFile keyFile, final CharSequence passphrase) throws BackgroundException {
        switch(keyFile.getVersion()) {
            case VAULT_VERSION:
                return keyFile;
            case 5:
                log.warn(String.format("Upgrade vault version %d to %d", keyFile.getVersion(), VAULT_VERSION));
                try {
                    final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(
                        FastSecureRandomProvider.get().provide()
                    );
                    final Cryptor cryptor = provider.createFromKeyFile(keyFile, passphrase, pepper, keyFile.getVersion());
                    // Create backup, as soon as we know the password was correct
                    final Path masterKeyFileBackup = new Path(home, DefaultVaultRegistry.DEFAULT_BACKUPKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault));
                    new ContentWriter(session).write(masterKeyFileBackup, keyFile.serialize());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Master key backup saved in %s", masterKeyFileBackup));
                    }
                    // Write updated masterkey file
                    final KeyFile upgradedMasterKeyFile = cryptor.writeKeysToMasterkeyFile(passphrase, pepper, VAULT_VERSION);
                    final Path masterKeyFile = new Path(home, DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault));
                    final byte[] masterKeyFileContent = upgradedMasterKeyFile.serialize();
                    new ContentWriter(session).write(masterKeyFile, masterKeyFileContent, new TransferStatus().exists(true).length(masterKeyFileContent.length));
                    log.warn(String.format("Updated masterkey %s to version %d", masterKeyFile, VAULT_VERSION));
                    return KeyFile.parse(upgradedMasterKeyFile.serialize());
                }
                catch(IllegalArgumentException e) {
                    throw new VaultException("Failure reading key file", e);
                }
                catch(InvalidPassphraseException e) {
                    throw new CryptoAuthenticationException("Failure to decrypt master key file", e);
                }
            default:
                log.error(String.format("Unsupported vault version %d", keyFile.getVersion()));
                return keyFile;
        }
    }

    private void open(final KeyFile keyFile, final CharSequence passphrase) throws VaultException, CryptoAuthenticationException {
        final CryptorProvider provider = new Version1CryptorModule().provideCryptorProvider(
            FastSecureRandomProvider.get().provide()
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Initialized crypto provider %s", provider));
        }
        try {
            cryptor = provider.createFromKeyFile(keyFile, new NFCNormalizer().normalize(passphrase), pepper, VAULT_VERSION);
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
        if(this.isUnlocked()) {
            return new SimplePathPredicate(file).test(home) || file.isChild(home);
        }
        return false;
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), false);
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file, boolean metadata) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), metadata);
    }

    public Path encrypt(final Session<?> session, final Path file, final String directoryId, boolean metadata) throws BackgroundException {
        final Path encrypted;
        if(file.isFile() || metadata) {
            if(file.getType().contains(Path.Type.vault)) {
                log.warn(String.format("Skip file %s because it is marked as an internal vault path", file));
                return file;
            }
            if(new SimplePathPredicate(file).test(home)) {
                log.warn(String.format("Skip vault home %s because the root has no metadata file", file));
                return file;
            }
            final Path parent;
            final String filename;
            if(file.getType().contains(Path.Type.encrypted)) {
                final Path decrypted = file.attributes().getDecrypted();
                parent = directoryProvider.toEncrypted(session, decrypted.getParent().attributes().getDirectoryId(), decrypted.getParent());
                filename = directoryProvider.toEncrypted(session, parent.attributes().getDirectoryId(), decrypted.getName(), decrypted.getType());
            }
            else {
                parent = directoryProvider.toEncrypted(session, file.getParent().attributes().getDirectoryId(), file.getParent());
                filename = directoryProvider.toEncrypted(session, parent.attributes().getDirectoryId(), file.getName(), file.getType());
            }
            final PathAttributes attributes = new PathAttributes(file.attributes());
            attributes.setDirectoryId(null);
            if(metadata) {
                // The directory is different from the metadata file used to resolve the actual folder
                attributes.setVersionId(null);
            }
            // Translate file size
            attributes.setSize(this.toCiphertextSize(file.attributes().getSize()));
            final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
            type.remove(Path.Type.directory);
            type.remove(Path.Type.decrypted);
            type.add(Path.Type.file);
            type.add(Path.Type.encrypted);
            encrypted = new Path(parent, filename, type, attributes);
        }
        else {
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn(String.format("Skip file %s because it is already marked as an encrypted path", file));
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                return directoryProvider.toEncrypted(session, home.attributes().getDirectoryId(), home);
            }
            encrypted = directoryProvider.toEncrypted(session, directoryId, file);
        }
        // Add reference to decrypted file
        encrypted.attributes().setDecrypted(file);
        // Add reference for vault
        file.attributes().setVault(home);
        encrypted.attributes().setVault(home);
        return encrypted;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path file) throws BackgroundException {
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
                final String cleartextFilename = cryptor.fileNameCryptor().decryptFilename(
                    ciphertext, file.getParent().attributes().getDirectoryId().getBytes(StandardCharsets.UTF_8));
                final PathAttributes attributes = new PathAttributes(file.attributes());
                if(inflated.getName().startsWith(DIR_PREFIX)) {
                    final Permission permission = attributes.getPermission();
                    permission.setUser(permission.getUser().or(Permission.Action.execute));
                    permission.setGroup(permission.getGroup().or(Permission.Action.execute));
                    permission.setOther(permission.getOther().or(Permission.Action.execute));
                    // Reset size for folders
                    attributes.setSize(-1L);
                    attributes.setVersionId(null);
                }
                else {
                    // Translate file size
                    attributes.setSize(this.toCleartextSize(file.attributes().getSize()));
                }
                // Add reference to encrypted file
                attributes.setEncrypted(file);
                // Add reference for vault
                attributes.setVault(home);
                final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
                type.remove(inflated.getName().startsWith(DIR_PREFIX) ? Path.Type.file : Path.Type.directory);
                type.add(inflated.getName().startsWith(DIR_PREFIX) ? Path.Type.directory : Path.Type.file);
                type.remove(Path.Type.encrypted);
                type.add(Path.Type.decrypted);
                final Path decrypted = new Path(file.getParent().attributes().getDecrypted(), cleartextFilename, type, attributes);
                if(type.contains(Path.Type.symboliclink)) {
                    decrypted.setSymlinkTarget(file.getSymlinkTarget());
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

    @Override
    public long toCiphertextSize(final long cleartextFileSize) {
        if(-1L == cleartextFileSize) {
            return -1L;
        }
        return cryptor.fileHeaderCryptor().headerSize() + Cryptors.ciphertextSize(cleartextFileSize, cryptor);
    }

    @Override
    public long toCleartextSize(final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
        if(-1L == ciphertextFileSize) {
            return -1L;
        }
        final int headerSize = cryptor.fileHeaderCryptor().headerSize();
        try {
            return Cryptors.cleartextSize(ciphertextFileSize - headerSize, cryptor);
        }
        catch(AssertionError e) {
            throw new CryptoInvalidFilesizeException(String.format("Encrypted file size must be at least %d bytes", headerSize));
        }
        catch(IllegalArgumentException e) {
            throw new CryptoInvalidFilesizeException(String.format("Invalid file size. %s", e.getMessage()));
        }
    }

    private Path inflate(final Session<?> session, final Path file) throws BackgroundException {
        final String fileName = file.getName();
        if(filenameProvider.isDeflated(fileName)) {
            final String filename = filenameProvider.inflate(session, fileName);
            return new Path(file.getParent(), filename, EnumSet.of(Path.Type.file), file.attributes());
        }
        return file;
    }

    public Path getHome() {
        return home;
    }

    public Path getMasterkey() {
        return masterkey;
    }

    public byte[] getPepper() {
        return pepper;
    }

    public Cryptor getCryptor() {
        return cryptor;
    }

    public CryptoFilenameProvider getFilenameProvider() {
        return filenameProvider;
    }

    public CryptoDirectoryProvider getDirectoryProvider() {
        return directoryProvider;
    }

    public int numberOfChunks(final long cleartextFileSize) {
        return (int) (cleartextFileSize / cryptor.fileContentCryptor().cleartextChunkSize() +
            ((cleartextFileSize % cryptor.fileContentCryptor().cleartextChunkSize() > 0) ? 1 : 0));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) {
        if(this.isUnlocked()) {
            if(type == ListService.class) {
                return (T) new CryptoListService(session, (ListService) delegate, this);
            }
            if(type == Touch.class) {
                // Use default touch feature because touch with remote implementation will not add encrypted file header
                return (T) new CryptoTouchFeature(session, new DefaultTouchFeature(session._getFeature(Upload.class), session._getFeature(AttributesFinder.class)), session._getFeature(Write.class), this);
            }
            if(type == Directory.class) {
                return (T) new CryptoDirectoryFeature(session, (Directory) delegate, session._getFeature(Write.class), this);
            }
            if(type == Upload.class) {
                return (T) new CryptoUploadFeature(session, (Upload) delegate, session._getFeature(Write.class), this);
            }
            if(type == Download.class) {
                return (T) new CryptoDownloadFeature(session, (Download) delegate, session._getFeature(Read.class), this);
            }
            if(type == Read.class) {
                return (T) new CryptoReadFeature(session, (Read) delegate, this);
            }
            if(type == Write.class) {
                return (T) new CryptoWriteFeature(session, (Write) delegate, this);
            }
            if(type == MultipartWrite.class) {
                return (T) new CryptoMultipartWriteFeature(session, (Write) delegate, this);
            }
            if(type == Move.class) {
                return (T) new CryptoMoveFeature(session, (Move) delegate, session._getFeature(Delete.class), this);
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
                return (T) new CryptoBulkFeature(session, (Bulk) delegate, session._getFeature(Delete.class), this);
            }
            if(type == UnixPermission.class) {
                return (T) new CryptoUnixPermission(session, (UnixPermission) delegate, this);
            }
            if(type == AclPermission.class) {
                return (T) new CryptoAclPermission(session, (AclPermission) delegate, this);
            }
            if(type == Copy.class) {
                return (T) new CryptoCopyFeature(session, (Copy) delegate, this);
            }
            if(type == Timestamp.class) {
                return (T) new CryptoTimestampFeature(session, (Timestamp) delegate, this);
            }
            if(type == Encryption.class) {
                return (T) new CryptoEncryptionFeature(session, (Encryption) delegate, this);
            }
            if(type == Lifecycle.class) {
                return (T) new CryptoLifecycleFeature(session, (Lifecycle) delegate, this);
            }
            if(type == Location.class) {
                return (T) new CryptoLocationFeature(session, (Location) delegate, this);
            }
            if(type == Lock.class) {
                return (T) new CryptoLockFeature(session, (Lock) delegate, this);
            }
            if(type == Logging.class) {
                return (T) new CryptoLoggingFeature(session, (Logging) delegate, this);
            }
            if(type == Redundancy.class) {
                return (T) new CryptoRedundancyFeature(session, (Redundancy) delegate, this);
            }
            if(type == Search.class) {
                return (T) new CryptoSearchFeature(session, (Search) delegate, this);
            }
            if(type == TransferAcceleration.class) {
                return (T) new CryptoTransferAccelerationFeature<>(session, (TransferAcceleration) delegate, this);
            }
            if(type == Versioning.class) {
                return (T) new CryptoVersioningFeature(session, (Versioning) delegate, this);
            }
            if(type == Home.class) {
                return (T) new CryptoHomeFeature(session, (Home) delegate, this);
            }
        }
        return delegate;
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
        return new SimplePathPredicate(home).test(that.home);
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
