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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;

import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;

public interface CryptoVaultInterface extends Vault {

    Path getMasterkey();

    Path getConfig();

    FileHeaderCryptor getFileHeaderCryptor();

    FileContentCryptor getFileContentCryptor();

    CryptorCache getFileNameCryptor();

    CryptoFilename getFilenameProvider();

    CryptoDirectory getDirectoryProvider();

    int getNonceSize();

    int numberOfChunks(long cleartextFileSize);

    long toCleartextSize(final long cleartextFileOffset, final long ciphertextFileSize) throws CryptoInvalidFilesizeException;

    @Override
    default Path encrypt(Session<?> session, Path file) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), false);
    }

    @Override
    default Path encrypt(Session<?> session, Path file, boolean metadata) throws BackgroundException {
        return this.encrypt(session, file, file.attributes().getDirectoryId(), metadata);
    }

    Path encrypt(Session<?> session, Path file, String directoryId, boolean metadata) throws BackgroundException;
}
