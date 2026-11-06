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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.features.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.UnlockedVault;
import org.cryptomator.cryptolib.api.VaultDirectory;
import org.cryptomator.cryptolib.api.VaultFile;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractVault implements Vault {
    private static final Logger log = LogManager.getLogger(AbstractVault.class);

    public abstract Path getMasterkeyPath();

    public abstract Path getConfig();

    public abstract CryptoFilename getFilenameProvider();

    public abstract CryptoDirectory getDirectoryProvider();

    /**
     * @return Unlocked vault or null when locked
     */
    public abstract UnlockedVault getUnlockedVault();

    /**
     * @return Length of chunk nonces in bytes
     */
    public abstract int getNonceSize();

    public abstract Pattern getFilenamePattern();

    /**
     * @return New encrypted header to be used for a single file
     */
    public ByteBuffer createHeader() {
        try(VaultFile file = this.getUnlockedVault().createFile()) {
            // Copy from read-only view to allow access to backing array
            final ByteBuffer encrypted = file.encryptedHeader();
            final ByteBuffer header = ByteBuffer.allocate(encrypted.remaining());
            header.put(encrypted);
            header.flip();
            return header;
        }
    }

    /**
     * Open file for encryption and decryption of chunks
     *
     * @param header Encrypted file header previously created with {@link #createHeader()} or read from the beginning of a file
     * @return File to encrypt and decrypt chunks with. Must be closed after use to destroy the file key.
     */
    public VaultFile openFile(final ByteBuffer header) throws CryptoAuthenticationException {
        try {
            // Size of empty file because the cleartext size is not used when working with chunks only
            return this.getUnlockedVault().openFile(header.duplicate(), this.getUnlockedVault().ciphertextSize(0L));
        }
        catch(AuthenticationFailedException e) {
            throw new CryptoAuthenticationException("Failure to decrypt file header", e);
        }
    }

    /**
     * @return Size of encrypted file header in bytes
     */
    public int getHeaderSize() {
        return this.getUnlockedVault().headerSize();
    }

    /**
     * @return Number of cleartext bytes per chunk
     */
    public int getCleartextChunkSize() {
        return this.getUnlockedVault().cleartextChunkSize();
    }

    /**
     * @return Number of ciphertext bytes per chunk
     */
    public int getCiphertextChunkSize() {
        return this.getUnlockedVault().ciphertextChunkSize();
    }

    /**
     * Number of chunks of a complete file. Includes a trailing empty chunk where required by the vault format.
     *
     * @param cleartextFileSize Size of complete file
     * @return Number of chunks that will be encrypted for the file
     */
    public int numberOfChunks(final long cleartextFileSize) {
        final long ciphertextBodySize = this.getUnlockedVault().ciphertextSize(cleartextFileSize) - this.getHeaderSize();
        return (int) ((ciphertextBodySize + this.getCiphertextChunkSize() - 1) / this.getCiphertextChunkSize());
    }

    /**
     * @param cleartextOffset Position in cleartext
     * @return Index of chunk containing given position
     */
    public long chunkIndex(final long cleartextOffset) {
        return cleartextOffset / this.getCleartextChunkSize();
    }

    /**
     * @param cleartextOffset Position in cleartext at the beginning of a chunk
     * @return Position of the chunk in ciphertext including the file header
     */
    public long toCiphertextOffset(final long cleartextOffset) {
        if(0L == cleartextOffset) {
            return 0L;
        }
        return this.getHeaderSize() + this.chunkIndex(cleartextOffset) * this.getCiphertextChunkSize();
    }

    /**
     * Ciphertext size of a segment of a file
     *
     * @param cleartextOffset Offset of segment in cleartext at the beginning of a chunk
     * @param cleartextSize   Length of segment
     * @param last            True for the last segment of a file which includes a trailing empty chunk where required by the vault format
     * @return Ciphertext size including the file header for the first segment
     */
    public long toCiphertextSize(final long cleartextOffset, final long cleartextSize, final boolean last) {
        if(last || TransferStatus.UNKNOWN_LENGTH == cleartextSize) {
            return this.toCiphertextSize(cleartextOffset, cleartextSize);
        }
        final long chunks = cleartextSize / this.getCleartextChunkSize();
        final long remainder = cleartextSize % this.getCleartextChunkSize();
        final long body = chunks * this.getCiphertextChunkSize() +
                (remainder > 0 ? remainder + this.getCiphertextChunkSize() - this.getCleartextChunkSize() : 0L);
        return (0L == cleartextOffset ? this.getHeaderSize() : 0L) + body;
    }

    public long toCleartextSize(final long cleartextFileOffset, final long ciphertextFileSize) throws CryptoInvalidFilesizeException {
        if(TransferStatus.UNKNOWN_LENGTH == ciphertextFileSize) {
            return TransferStatus.UNKNOWN_LENGTH;
        }
        try {
            if(0L == cleartextFileOffset) {
                return this.getUnlockedVault().cleartextSize(ciphertextFileSize);
            }
            // Partial content without header
            return this.getUnlockedVault().cleartextSize(ciphertextFileSize + this.getHeaderSize());
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
        final long ciphertextFileSize = this.getUnlockedVault().ciphertextSize(cleartextFileSize);
        if(0L == cleartextFileOffset) {
            return ciphertextFileSize;
        }
        // Partial content without header
        return ciphertextFileSize - this.getHeaderSize();
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return this.encrypt(session, file, false);
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file, final boolean metadata) throws BackgroundException {
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
                parent = this.getDirectoryProvider().toEncrypted(session, decrypted.getParent());
                filename = this.getDirectoryProvider().toEncrypted(session, decrypted.getParent(), decrypted.getName(), decrypted.getType());
            }
            else {
                parent = this.getDirectoryProvider().toEncrypted(session, file.getParent());
                filename = this.getDirectoryProvider().toEncrypted(session, file.getParent(), file.getName(), file.getType());
            }
            final PathAttributes attributes = new DefaultPathAttributes(file.attributes());
            attributes.setDirectoryId(null);
            if(!file.isFile() && !metadata) {
                // The directory is different from the metadata file used to resolve the actual folder
                attributes.setVersionId(null);
                attributes.setFileId(null);
            }
            // Translate file size
            attributes.setSize(this.toCiphertextSize(0L, file.attributes().getSize()));
            final EnumSet<Path.Type> type = EnumSet.copyOf(file.getType());
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
                return this.getDirectoryProvider().toEncrypted(session, this.getHome());
            }
            encrypted = this.getDirectoryProvider().toEncrypted(session, file);
        }
        // Add reference to decrypted file
        if(!file.getType().contains(Path.Type.encrypted)) {
            encrypted.attributes().setDecrypted(file);
        }
        return encrypted;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path file) throws BackgroundException {
        if(file.getType().contains(Path.Type.decrypted)) {
            log.warn("Skip file {} because it is already marked as a decrypted path", file);
            return file;
        }
        if(file.getType().contains(Path.Type.vault)) {
            log.warn("Skip file {} because it is marked as an internal vault path", file);
            return file;
        }
        final Path inflated = this.inflate(session, file);
        final Pattern pattern = this.getFilenamePattern();
        final Matcher m = pattern.matcher(inflated.getName());
        if(m.matches()) {
            try {
                final String cleartextFilename = this.getParentDirectory(session, file).decryptName(inflated.getName());
                final PathAttributes attributes = new DefaultPathAttributes(file.attributes());
                if(this.isDirectory(inflated)) {
                    if(Permission.EMPTY != attributes.getPermission()) {
                        final StaticPermission permission = new StaticPermission(attributes.getPermission());
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
                throw new CryptoAuthenticationException("Failure to decrypt due to an unauthentic ciphertext", e).withFile(file);
            }
            catch(IllegalArgumentException e) {
                throw new CryptoFilenameMismatchException(String.format("Failure to decrypt %s. %s", inflated.getName(), e.getMessage())).withFile(file);
            }
        }
        else {
            throw new CryptoFilenameMismatchException(String.format("Failure to decrypt %s due to missing pattern match for %s", inflated.getName(), pattern)).withFile(file);
        }
    }

    private VaultDirectory getParentDirectory(final Session<?> session, final Path file) throws BackgroundException {
        // Read directory id from file
        log.debug("Read directory ID for parent of {}", file);
        return this.getDirectoryProvider().getDirectoryId(session, file.getParent());
    }

    private boolean isDirectory(final Path p) {
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
        return this.getUnlockedVault() != null;
    }

    @Override
    public boolean contains(final Path file) {
        if(this.isUnlocked()) {
            return new SimplePathPredicate(file).test(this.getHome()) || file.isChild(this.getHome());
        }
        return false;
    }

    public abstract String getDirectoryMetadataFilename();

    public abstract String getBackupDirectoryMetadataFilename();

    public abstract VaultDirectory getRootDirId();

    @Override
    public synchronized void close() {
        if(this.isUnlocked()) {
            if(this.getUnlockedVault() != null) {
                this.getUnlockedVault().destroy();
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
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) throws UnsupportedException {
        if(this.isUnlocked()) {
            if(type == ListService.class) {
                return (T) new CryptoListService(session, (ListService) delegate, this);
            }
            if(type == Touch.class) {
                // Use default touch feature because touch with remote implementation will not add encrypted file header
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
            if(type == MultipartWrite.class) {
                return (T) new CryptoMultipartWriteFeature(session, (Write) delegate, this);
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
            if(type == FileIdProvider.class) {
                return (T) new CryptoFileIdProvider(session, (FileIdProvider) delegate, this);
            }
            if(type == VersionIdProvider.class) {
                return (T) new CryptoVersionIdProvider(session, (VersionIdProvider) delegate, this);
            }
            if(type == Delete.class) {
                return (T) new CryptoDeleteFeature(session, (Delete) delegate, this);
            }
            if(type == Trash.class) {
                return (T) new CryptoDeleteFeature(session, (Delete) delegate, this);
            }
            if(type == Symlink.class) {
                return (T) new CryptoSymlinkFeature(session, (Symlink) delegate, this);
            }
            if(type == Headers.class || type == Metadata.class) {
                return (T) new CryptoHeadersFeature(session, (Headers) delegate, this);
            }
            if(type == Compress.class) {
                return (T) new CryptoCompressFeature(session, (Compress) delegate, this);
            }
            if(type == Bulk.class) {
                return (T) new CryptoBulkFeature(session, (Bulk) delegate, this);
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
