package ch.cyberduck.core.cryptomator.impl;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

public class CryptoDirectoryV7Provider extends CryptoDirectoryV6Provider {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV7Provider.class);

    private final CryptoFilename filenameProvider;
    private final CryptorCache filenameCryptor;
    private final AbstractVault vault;

    private final RandomStringService random
            = new UUIDRandomStringService();

    public CryptoDirectoryV7Provider(final AbstractVault vault, final CryptoFilename filenameProvider, final CryptorCache filenameCryptor) {
        super(vault.getHome(), filenameProvider, filenameCryptor);
        this.filenameProvider = filenameProvider;
        this.filenameCryptor = filenameCryptor;
        this.vault = vault;
    }

    @Override
    public String toEncrypted(final Session<?> session, final Path parent, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final String ciphertextName = filenameCryptor.encryptFilename(BaseEncoding.base64Url(), filename, this.getOrCreateDirectoryId(session, parent)) + vault.getRegularFileExtension();
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return filenameProvider.deflate(session, ciphertextName);
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path encryptedParent = this.toEncrypted(session, directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, directory.getParent(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(encryptedParent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(metadataParent, vault.getDirectoryMetadataFilename(), EnumSet.of(Path.Type.file, Path.Type.encrypted));
            return new ContentReader(session).readBytes(metadataFile);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return random.random().getBytes(StandardCharsets.US_ASCII);
        }
    }
}
