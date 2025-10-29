package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.DirectoryMetadata;

import java.util.EnumSet;

public class CryptoDirectoryUVFProvider extends CryptoDirectoryV8Provider {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryUVFProvider.class);

    private final Path home;
    private final AbstractVault vault;
    private final CryptoFilename filenameProvider;

    public CryptoDirectoryUVFProvider(final AbstractVault vault, final CryptoFilename filenameProvider) {
        super(vault, filenameProvider);
        this.filenameProvider = filenameProvider;
        this.home = vault.getHome();
        this.vault = vault;
    }

    //TODO kann das auch ersetzt werden mit der impl der superklasse? hier wird load verwendet? nötig?
    @Override
    public String toEncrypted(final Session<?> session, final Path parent, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final DirectoryMetadata dirMetadata = load(session, parent);
        this.vault.getCryptor().directoryContentCryptor().fileNameEncryptor(dirMetadata).encrypt(filename);
        final String ciphertextName = this.vault.getCryptor().directoryContentCryptor().fileNameEncryptor(dirMetadata).encrypt(filename);
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return filenameProvider.deflate(session, ciphertextName);
    }

    protected DirectoryMetadata load(final Session<?> session, final Path directory) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return vault.getRootDirId();
        }
        final Path parent = this.toEncrypted(session, directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, directory.getParent(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(parent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(metadataParent, vault.getDirectoryMetadataFilename(), EnumSet.of(Path.Type.file, Path.Type.encrypted));
            final byte[] ciphertext = new ContentReader(session).readBytes(metadataFile);
            return this.vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(ciphertext);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return this.getOrCreateDirectoryId(session, directory);
        }
    }
}
