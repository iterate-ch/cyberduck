package ch.cyberduck.core.cryptomator.legacy.features;

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
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CryptoDirectoryV6Feature<Reply> implements Directory<Reply> {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV6Feature.class);

    private final Session<?> session;
    private final Directory<Reply> delegate;
    private final CryptomatorVault cryptomator;

    public CryptoDirectoryV6Feature(final Session<?> session, final Directory<Reply> delegate, final CryptomatorVault cryptomator) {
        this.session = session;
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public Path mkdir(final Write<Reply> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        final byte[] directoryId = cryptomator.getDirectoryProvider().createDirectoryId(folder);
        final Path encrypt = cryptomator.encrypt(session, folder, directoryId, false);
        // Create metadata file for directory
        final Path directoryMetadataFile = cryptomator.encrypt(session, folder, true);
        log.debug("Write metadata {} for folder {}", directoryMetadataFile, folder);
        directoryMetadataFile.setAttributes(new ContentWriter(session).write(directoryMetadataFile, directoryId, new TransferStatus()));
        final Path intermediate = encrypt.getParent();
        if(!session._getFeature(Find.class).find(intermediate)) {
            session._getFeature(Directory.class).mkdir(session._getFeature(Write.class), intermediate, new TransferStatus().setRegion(status.getRegion()));
        }
        final Path target = delegate.mkdir(writer, encrypt, status);
        // Implementation may return new copy of attributes without encryption attributes
        target.attributes().setDirectoryId(directoryId);
        target.attributes().setDecrypted(folder);
        // Make reference of encrypted path in attributes of decrypted file point to metadata file
        final Path decrypt = cryptomator.decrypt(session, cryptomator.encrypt(session, target, true));
        decrypt.attributes().setFileId(directoryMetadataFile.attributes().getFileId());
        decrypt.attributes().setVersionId(directoryMetadataFile.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public boolean isSupported(final Path workdir, final Optional<String> name) {
        return delegate.isSupported(workdir, name);
    }

    @Override
    public void preflight(final Path workdir, final Optional<String> filename) throws BackgroundException {
        delegate.preflight(cryptomator.encrypt(session, workdir), filename);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDirectoryFeature{");
        sb.append("proxy=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
