package ch.cyberduck.core.cryptomator.features;

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
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.DirectoryMetadata;

import java.util.EnumSet;

public class CryptoDirectoryUVFFeature<Reply> extends CryptoDirectoryV7Feature<Reply> {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryUVFFeature.class);

    private final Session<?> session;
    private final AbstractVault vault;

    public CryptoDirectoryUVFFeature(final Session<?> session, final Directory<Reply> delegate, final AbstractVault vault) {
        super(session, delegate, vault);
        this.session = session;
        this.vault = vault;
    }

    protected void writeRecoveryMetadata(final Path folder, final Path target, final DirectoryMetadata metadata) throws BackgroundException {
        final Path recoveryDirectoryMetadataFile = new Path(target,
                vault.getDirectoryMetadataFilename(),
                EnumSet.of(Path.Type.file));
        log.debug("Write recovery metadata {} for folder {}", recoveryDirectoryMetadataFile, folder);
        new ContentWriter(session).write(recoveryDirectoryMetadataFile, this.vault.getCryptor().directoryContentCryptor().encryptDirectoryMetadata(metadata));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDirectoryUVFFeature{");
        sb.append("vault=").append(vault);
        sb.append('}');
        return sb.toString();
    }
}
