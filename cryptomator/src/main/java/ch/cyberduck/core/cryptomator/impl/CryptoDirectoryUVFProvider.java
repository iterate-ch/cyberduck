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
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.FileHeader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class CryptoDirectoryUVFProvider extends CryptoDirectoryV7Provider {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryUVFProvider.class);

    private final Path home;
    private final AbstractVault vault;

    private final RandomStringService random
            = new UUIDRandomStringService();

    public CryptoDirectoryUVFProvider(final AbstractVault vault, final CryptoFilename filenameProvider, final CryptorCache filenameCryptor) {
        super(vault, filenameProvider, filenameCryptor);
        this.home = vault.getHome();
        this.vault = vault;
    }

    @Override
    protected byte[] toDirectoryId(final Session<?> session, final Path directory, final byte[] directoryId) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return vault.getRootDirId();
        }
        return super.toDirectoryId(session, directory, directoryId);
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path parent = this.toEncrypted(session, directory.getParent().attributes().getDirectoryId(), directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(parent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(metadataParent, vault.getDirectoryMetadataFilename(), EnumSet.of(Path.Type.file, Path.Type.encrypted));
            final byte[] ciphertext = new ContentReader(session).readBytes(metadataFile);
            // https://github.com/encryption-alliance/unified-vault-format/blob/develop/file%20name%20encryption/AES-SIV-512-B64URL.md#format-of-diruvf-and-symlinkuvf
            // TODO can we not use org.cryptomator.cryptolib.v3.DirectoryContentCryptorImpl.decryptDirectoryMetadata()? DirectoryMetadataImpl is not visible and DirectoryMetadata is empty interface, so we cannot access dirId attribute.
            if(ciphertext.length != 128) {
                throw new IllegalArgumentException("Invalid dir.uvf length: " + ciphertext.length);
            }
            int headerSize = vault.getCryptor().fileHeaderCryptor().headerSize();
            ByteBuffer buffer = ByteBuffer.wrap(ciphertext);
            ByteBuffer headerBuf = buffer.duplicate();
            headerBuf.position(0).limit(headerSize);
            ByteBuffer contentBuf = buffer.duplicate();
            contentBuf.position(headerSize);

            FileHeader header = vault.getCryptor().fileHeaderCryptor().decryptHeader(headerBuf);
            ByteBuffer plaintext = vault.getCryptor().fileContentCryptor().decryptChunk(contentBuf, 0, header, true);
            assert plaintext.remaining() == 32;
            byte[] dirId = new byte[32];
            plaintext.get(dirId);
            return dirId;
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return random.random().getBytes(StandardCharsets.US_ASCII);
        }
    }
}
