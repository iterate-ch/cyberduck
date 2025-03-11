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
import ch.cyberduck.core.PathAttributes;
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
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

public class CryptoDirectoryUVFProvider extends CryptoDirectoryV7Provider {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryUVFProvider.class);

    private final Path home;
    private final AbstractVault vault;

    private final RandomStringService random
            = new UUIDRandomStringService();
    private final Path dataRoot;
    private final CryptorCache filenameCryptor;
    private final CryptoFilename filenameProvider;

    public CryptoDirectoryUVFProvider(final AbstractVault vault, final CryptoFilename filenameProvider, final CryptorCache filenameCryptor) {
        super(vault, filenameProvider, filenameCryptor);
        this.filenameCryptor = filenameCryptor;
        this.filenameProvider = filenameProvider;
        this.home = vault.getHome();
        this.vault = vault;
        this.dataRoot = new Path(home, "d", home.getType());
    }

    @Override
    protected byte[] toDirectoryId(final Session<?> session, final Path directory, final byte[] directoryId) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return vault.getRootDirId();
        }
        return super.toDirectoryId(session, directory, directoryId);
    }

    // interface mismatch: we need parent path to get dirId and revision from dir.uvf
    public String toEncrypted(final Session<?> session, final Path parent, final String filename) throws BackgroundException {
        if(new SimplePathPredicate(home).test(parent)) {
            final String ciphertextName = filenameCryptor.encryptFilename(BaseEncoding.base64Url(), filename, vault.getRootDirId()) + vault.getRegularFileExtension();
            log.debug("Encrypted filename {} to {}", filename, ciphertextName);
            return filenameProvider.deflate(session, ciphertextName);

        }
        final byte[] directoryId = load(session, parent);
        final String ciphertextName = vault.getCryptor().fileNameCryptor(loadRevision(session, parent)).encryptFilename(BaseEncoding.base64Url(), filename, directoryId) + vault.getRegularFileExtension();
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return filenameProvider.deflate(session, ciphertextName);
    }

    @Override
    public Path toEncrypted(final Session<?> session, final byte[] directoryId, final Path directory) throws BackgroundException {
        if(!directory.isDirectory()) {
            throw new NotfoundException(directory.getAbsolute());
        }
        if(new SimplePathPredicate(directory).test(home) || directory.isChild(home)) {
            final PathAttributes attributes = new PathAttributes(directory.attributes());
            // The root of the vault is a different target directory and file ids always correspond to the metadata file
            attributes.withVersionId(null);
            attributes.withFileId(null);
            // Remember random directory id for use in vault
            final byte[] id = this.toDirectoryId(session, directory, directoryId);
            log.debug("Use directory ID '{}' for folder {}", id, directory);
            attributes.setDirectoryId(id);
            attributes.setDecrypted(directory);
            final String directoryIdHash;
            if(new SimplePathPredicate(home).test(directory)) {
                // TODO hard-coded to initial seed in UVFVault
                directoryIdHash = filenameCryptor.hashDirectoryId(id);
            }
            else {
                directoryIdHash = vault.getCryptor().fileNameCryptor(loadRevision(session, directory)).hashDirectoryId(id);
            }
            // Intermediate directory
            final Path intermediate = new Path(dataRoot, directoryIdHash.substring(0, 2), dataRoot.getType());
            // Add encrypted type
            final EnumSet<Path.Type> type = EnumSet.copyOf(directory.getType());
            type.add(Path.Type.encrypted);
            type.remove(Path.Type.decrypted);
            return new Path(intermediate, directoryIdHash.substring(2), type, attributes);
        }
        throw new NotfoundException(directory.getAbsolute());
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return vault.getRootDirId();
        }
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

            FileHeader header = vault.getCryptor().fileHeaderCryptor(loadRevision(session, directory)).decryptHeader(headerBuf);
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

    protected int loadRevision(final Session<?> session, final Path directory) throws BackgroundException {
        final Path parent = this.toEncrypted(session, directory.getParent().attributes().getDirectoryId(), directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(parent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
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
        headerBuf.position(4).limit(headerSize);
        return headerBuf.order(ByteOrder.BIG_ENDIAN).getInt();
    }
}
