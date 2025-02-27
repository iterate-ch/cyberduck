package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.features.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.BaseEncoding;

public abstract class AbstractVault implements Vault {

    private static final Logger log = LogManager.getLogger(AbstractVault.class);

    public static final int VAULT_VERSION_DEPRECATED = 6;
    public static final int VAULT_VERSION = PreferencesFactory.get().getInteger("cryptomator.vault.version");

    public static final String DIR_PREFIX = "0";

    private static final Pattern BASE32_PATTERN = Pattern.compile("^0?(([A-Z2-7]{8})*[A-Z2-7=]{8})");

    public abstract Path getMasterkey();

    public abstract Path getConfig();

    public abstract int getVersion();

    public abstract FileHeaderCryptor getFileHeaderCryptor();

    public abstract FileContentCryptor getFileContentCryptor();

    public abstract CryptorCache getFileNameCryptor();

    public abstract CryptoFilename getFilenameProvider();

    public abstract CryptoDirectory getDirectoryProvider();

    public abstract Cryptor getCryptor();

    public abstract int getNonceSize();

    public int numberOfChunks(long cleartextFileSize) {
        return (int) (cleartextFileSize / this.getFileContentCryptor().cleartextChunkSize() +
                ((cleartextFileSize % this.getFileContentCryptor().cleartextChunkSize() > 0) ? 1 : 0));
    }

    public long toCleartextSize(final long cleartextFileOffset, final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
        if(TransferStatus.UNKNOWN_LENGTH == ciphertextFileSize) {
            return TransferStatus.UNKNOWN_LENGTH;
        }
        final int headerSize;
        if(0L == cleartextFileOffset) {
            headerSize = this.getFileHeaderCryptor().headerSize();
        }
        else {
            headerSize = 0;
        }
        try {
            return this.getFileContentCryptor().cleartextSize(ciphertextFileSize - headerSize);
        }
        catch(AssertionError e) {
            throw new CryptoInvalidFilesizeException(String.format("Encrypted file size must be at least %d bytes", headerSize));
        }
        catch(IllegalArgumentException e) {
            throw new CryptoInvalidFilesizeException(String.format("Invalid file size. %s", e.getMessage()));
        }
    }

    @Override
    public State getState() {
        return this.isUnlocked() ? State.open : State.closed;
    }

    @Override
    public long toCiphertextSize(final long cleartextFileOffset, final long cleartextFileSize) {
        if(TransferStatus.UNKNOWN_LENGTH == cleartextFileSize) {
            return TransferStatus.UNKNOWN_LENGTH;
        }
        final int headerSize;
        if(0L == cleartextFileOffset) {
            headerSize = this.getCryptor().fileHeaderCryptor().headerSize();
        }
        else {
            headerSize = 0;
        }
        return headerSize + this.getCryptor().fileContentCryptor().ciphertextSize(cleartextFileSize);
    }

    @Override
    public Path encrypt(Session<?> session, Path file) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), false);
    }

    @Override
    public Path encrypt(Session<?> session, Path file, boolean metadata) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), metadata);
    }

    public Path encrypt(Session<?> session, Path file, byte[] directoryId, boolean metadata) throws BackgroundException {
        final Path encrypted;
        if(file.isFile() || metadata) {
            if(file.getType().contains(Path.Type.vault)) {
                log.warn("Skip file {} because it is marked as an internal vault path", file);
                return file;
            }
            if(new SimplePathPredicate(file).test(this.getHome())) {
                log.warn("Skip vault home {} because the root has no metadata file", file);
                return file;
            }
            final Path parent;
            final String filename;
            if(file.getType().contains(Path.Type.encrypted)) {
                final Path decrypted = file.attributes().getDecrypted();
                parent = this.getDirectoryProvider().toEncrypted(session, decrypted.getParent().attributes().getDirectoryId(), decrypted.getParent());
                filename = this.getDirectoryProvider().toEncrypted(session, parent.attributes().getDirectoryId(), decrypted.getName(), decrypted.getType());
            }
            else {
                parent = this.getDirectoryProvider().toEncrypted(session, file.getParent().attributes().getDirectoryId(), file.getParent());
                filename = this.getDirectoryProvider().toEncrypted(session, parent.attributes().getDirectoryId(), file.getName(), file.getType());
            }
            final PathAttributes attributes = new PathAttributes(file.attributes());
            attributes.setDirectoryId(null);
            if(!file.isFile() && !metadata) {
                // The directory is different from the metadata file used to resolve the actual folder
                attributes.setVersionId(null);
                attributes.setFileId(null);
            }
            // Translate file size
            attributes.setSize(this.toCiphertextSize(0L, file.attributes().getSize()));
            final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
            if(metadata && this.getVersion() == VAULT_VERSION_DEPRECATED) {
                type.remove(Path.Type.directory);
                type.add(Path.Type.file);
            }
            type.remove(Path.Type.decrypted);
            type.add(Path.Type.encrypted);
            encrypted = new Path(parent, filename, type, attributes);
        }
        else {
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn("Skip file {} because it is already marked as an encrypted path", file);
                return file;
            }
            if(file.getType().contains(Path.Type.vault)) {
                return this.getDirectoryProvider().toEncrypted(session, this.getHome().attributes().getDirectoryId(), this.getHome());
            }
            encrypted = this.getDirectoryProvider().toEncrypted(session, directoryId, file);
        }
        // Add reference to decrypted file
        if(!file.getType().contains(Path.Type.encrypted)) {
            encrypted.attributes().setDecrypted(file);
        }
        // Add reference for vault
        file.attributes().setVault(this.getHome());
        encrypted.attributes().setVault(this.getHome());
        return encrypted;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path file) throws BackgroundException {
        if(file.getType().contains(Path.Type.decrypted)) {
            log.warn("Skip file {} because it is already marked as an decrypted path", file);
            return file;
        }
        if(file.getType().contains(Path.Type.vault)) {
            log.warn("Skip file {} because it is marked as an internal vault path", file);
            return file;
        }
        final Path inflated = this.inflate(session, file);
        final Pattern pattern = this.getVersion() == VAULT_VERSION_DEPRECATED ? BASE32_PATTERN : this.getBase64URLPattern();
        final Matcher m = pattern.matcher(inflated.getName());
        if(m.matches()) {
            final String ciphertext = m.group(1);
            try {
                final String cleartextFilename = this.getFileNameCryptor().decryptFilename(
                        this.getVersion() == VAULT_VERSION_DEPRECATED ? BaseEncoding.base32() : BaseEncoding.base64Url(),
                        ciphertext, file.getParent().attributes().getDirectoryId());
                final PathAttributes attributes = new PathAttributes(file.attributes());
                if(this.isDirectory(inflated)) {
                    if(Permission.EMPTY != attributes.getPermission()) {
                        final Permission permission = new Permission(attributes.getPermission());
                        permission.setUser(permission.getUser().or(Permission.Action.execute));
                        permission.setGroup(permission.getGroup().or(Permission.Action.execute));
                        permission.setOther(permission.getOther().or(Permission.Action.execute));
                        attributes.setPermission(permission);
                    }
                    // Reset size for folders
                    attributes.setSize(-1L);
                    attributes.setVersionId(null);
                    attributes.setFileId(null);
                }
                else {
                    // Translate file size
                    attributes.setSize(this.toCleartextSize(0L, file.attributes().getSize()));
                }
                // Add reference to encrypted file
                attributes.setEncrypted(file);
                // Add reference for vault
                attributes.setVault(this.getHome());
                final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
                type.remove(this.isDirectory(inflated) ? Path.Type.file : Path.Type.directory);
                type.add(this.isDirectory(inflated) ? Path.Type.directory : Path.Type.file);
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
                    String.format("Failure to decrypt %s due to missing pattern match for %s", inflated.getName(), pattern));
        }
    }

    private boolean isDirectory(final Path p) {
        if(this.getVersion() == VAULT_VERSION_DEPRECATED) {
            return p.getName().startsWith(DIR_PREFIX);
        }
        return p.isDirectory();
    }

    private Path inflate(final Session<?> session, final Path file) throws BackgroundException {
        final String fileName = file.getName();
        if(this.getFilenameProvider().isDeflated(fileName)) {
            final String filename = this.getFilenameProvider().inflate(session, fileName);
            return new Path(file.getParent(), filename, EnumSet.of(Path.Type.file), file.attributes());
        }
        return file;
    }

    public synchronized boolean isUnlocked() {
        return this.getCryptor() != null;
    }

    @Override
    public boolean contains(final Path file) {
        if(this.isUnlocked()) {
            return new SimplePathPredicate(file).test(this.getHome()) || file.isChild(this.getHome());
        }
        return false;
    }

    public abstract String getRegularFileExtension();

    public abstract String getDirectoryMetadataFilename();

    public abstract String getBackupDirectoryMetadataFilename();

    public abstract Pattern getBase64URLPattern();

    @Override
    public synchronized void close() {
        if(this.isUnlocked()) {
            if(this.getCryptor() != null) {
                getCryptor().destroy();
            }
            if(this.getDirectoryProvider() != null) {
                this.getDirectoryProvider().destroy();
            }
            if(this.getFilenameProvider() != null) {
                this.getFilenameProvider().destroy();
            }
        }
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
                return (T) new CryptoTouchFeature(session, new DefaultTouchFeature(session._getFeature(Write.class)), session._getFeature(Write.class), this);
            }
            if(type == Directory.class) {
                return (T) (this.getVersion() == VAULT_VERSION_DEPRECATED ?
                        new CryptoDirectoryV6Feature(session, (Directory) delegate, session._getFeature(Write.class), this) :
                        new CryptoDirectoryV7Feature(session, (Directory) delegate, session._getFeature(Write.class), this)
                );
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
                return (T) (this.getVersion() == VAULT_VERSION_DEPRECATED ?
                        new CryptoMoveV6Feature(session, (Move) delegate, this) :
                        new CryptoMoveV7Feature(session, (Move) delegate, this));

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
            if(type == FileIdProvider.class) {
                return (T) new CryptoFileIdProvider(session, (FileIdProvider) delegate, this);
            }
            if(type == VersionIdProvider.class) {
                return (T) new CryptoVersionIdProvider(session, (VersionIdProvider) delegate, this);
            }
            if(type == Delete.class) {
                return (T) (this.getVersion() == VAULT_VERSION_DEPRECATED ?
                        new CryptoDeleteV6Feature(session, (Delete) delegate, this) :
                        new CryptoDeleteV7Feature(session, (Delete) delegate, this));
            }
            if(type == Trash.class) {
                return (T) (this.getVersion() == VAULT_VERSION_DEPRECATED ?
                        new CryptoDeleteV6Feature(session, (Delete) delegate, this) :
                        new CryptoDeleteV7Feature(session, (Delete) delegate, this));
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
        }
        return delegate;
    }
}
