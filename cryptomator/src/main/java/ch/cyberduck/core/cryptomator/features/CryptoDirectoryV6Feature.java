package ch.cyberduck.core.cryptomator.features;

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
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoVaultInterface;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.FileHeader;

import java.nio.charset.StandardCharsets;

public class CryptoDirectoryV6Feature<Reply> implements Directory<Reply> {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV6Feature.class);

    private final Session<?> session;
    private final Write<Reply> writer;
    private final Directory<Reply> delegate;
    private final CryptoVaultInterface vault;
    private final RandomStringService random = new UUIDRandomStringService();

    public CryptoDirectoryV6Feature(final Session<?> session, final Directory<Reply> delegate,
                                    final Write<Reply> writer, final CryptoVaultInterface cryptomator) {
        this.session = session;
        this.writer = writer;
        this.delegate = delegate;
        this.vault = cryptomator;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        final Path encrypt = vault.encrypt(session, folder, random.random(), false);
        final String directoryId = encrypt.attributes().getDirectoryId();
        // Create metadata file for directory
        final Path directoryMetadataFile = vault.encrypt(session, folder, true);
        log.debug("Write metadata {} for folder {}", directoryMetadataFile, folder);
        new ContentWriter(session).write(directoryMetadataFile, directoryId.getBytes(StandardCharsets.UTF_8));
        final Path intermediate = encrypt.getParent();
        if(!session._getFeature(Find.class).find(intermediate)) {
            session._getFeature(Directory.class).mkdir(intermediate, new TransferStatus().setRegion(status.getRegion()));
        }
        // Write header
        final FileHeader header = vault.getFileHeaderCryptor().create();
        status.setHeader(vault.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator(vault.getNonceSize()));
        final Path target = delegate.withWriter(new CryptoWriteFeature<>(session, writer, vault)).mkdir(encrypt, status);
        // Implementation may return new copy of attributes without encryption attributes
        target.attributes().setDirectoryId(directoryId);
        target.attributes().setDecrypted(folder);
        // Make reference of encrypted path in attributes of decrypted file point to metadata file
        final Path decrypt = vault.decrypt(session, vault.encrypt(session, target, true));
        decrypt.attributes().setFileId(directoryMetadataFile.attributes().getFileId());
        decrypt.attributes().setVersionId(directoryMetadataFile.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return delegate.isSupported(workdir, name);
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        delegate.preflight(vault.encrypt(session, workdir), filename);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDirectoryFeature{");
        sb.append("proxy=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
