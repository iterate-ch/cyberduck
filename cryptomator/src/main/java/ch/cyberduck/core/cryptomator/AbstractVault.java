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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cryptomator.features.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;

public abstract class AbstractVault implements Vault {

    public static final int VAULT_VERSION_DEPRECATED = 6;
    public static final int VAULT_VERSION = PreferencesFactory.get().getInteger("cryptomator.vault.version");

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
    public Path encrypt(Session<?> session, Path file) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), false);
    }

    @Override
    public Path encrypt(Session<?> session, Path file, boolean metadata) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), metadata);
    }

    public abstract Path encrypt(Session<?> session, Path file, String directoryId, boolean metadata) throws BackgroundException;

    public synchronized boolean isUnlocked() {
        return this.getCryptor() != null;
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
