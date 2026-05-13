package ch.cyberduck.core.cryptomator.legacy.impl;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

public class CryptoDirectoryV7Provider extends CryptoDirectoryV6Provider {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV7Provider.class);

    public static final String EXTENSION_REGULAR = ".c9r";
    public static final String FILENAME_DIRECTORYID = "dir";
    public static final String DIRECTORY_METADATAFILE = String.format("%s%s", FILENAME_DIRECTORYID, EXTENSION_REGULAR);
    public static final String BACKUP_FILENAME_DIRECTORYID = "dirid";
    public static final String BACKUP_DIRECTORY_METADATAFILE = String.format("%s%s", BACKUP_FILENAME_DIRECTORYID, EXTENSION_REGULAR);

    private final CryptomatorVault cryptomator;

    public CryptoDirectoryV7Provider(final Path vault, final CryptomatorVault cryptomator) {
        super(vault, cryptomator);
        this.cryptomator = cryptomator;
    }

    @Override
    public String toEncrypted(final Session<?> session, final byte[] directoryId, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final String ciphertextName = cryptomator.getFileNameCryptor().encryptFilename(BaseEncoding.base64Url(), filename, directoryId) + EXTENSION_REGULAR;
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return cryptomator.getFilenameProvider().deflate(session, ciphertextName);
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path parent = this.toEncrypted(session, directory.getParent().attributes().getDirectoryId(), directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(parent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
        final Path metadataFile = new Path(metadataParent, CryptoDirectoryV7Provider.DIRECTORY_METADATAFILE, EnumSet.of(Path.Type.file, Path.Type.encrypted));
        return new ContentReader(session).readBytes(metadataFile);
    }
}
