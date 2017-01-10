package ch.cyberduck.core.cryptomator.impl;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class CryptoDirectoryProvider {

    private static final String DATA_DIR_NAME = "d";
    private static final String ROOT_DIR_ID = StringUtils.EMPTY;

    private final Path dataRoot;
    private final CryptoVault cryptomator;

    public CryptoDirectoryProvider(final Path vault, final CryptoVault cryptomator) {
        this.dataRoot = new Path(vault, DATA_DIR_NAME, EnumSet.of(Path.Type.directory, Path.Type.vault));
        this.cryptomator = cryptomator;
    }

    /**
     * @param directoryId Directory id
     * @param filename    Clear text filename
     * @param type        File type
     * @return Encrypted filename
     */
    public String toEncrypted(final Session<?> session, final String directoryId, final String filename, final EnumSet<AbstractPath.Type> type) throws BackgroundException {
        final String prefix = type.contains(Path.Type.directory) ? CryptoVault.DIR_PREFIX : "";
        final String ciphertextName = String.format("%s%s", prefix,
                cryptomator.getCryptor().fileNameCryptor().encryptFilename(filename, directoryId.getBytes(StandardCharsets.UTF_8)));
        return cryptomator.getFilenameProvider().deflate(session, ciphertextName);
    }

    /**
     * @param directory Clear text
     */
    public Path toEncrypted(final Session<?> session, final Path directory) throws BackgroundException {
        if(dataRoot.getParent().equals(directory)) {
            return this.resolve(ROOT_DIR_ID);
        }
        final Path parent = this.toEncrypted(session, directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        final String dirId = cryptomator.getDirectoryIdProvider().load(session, new Path(parent, ciphertextName, EnumSet.of(Path.Type.file, Path.Type.encrypted)));
        return this.resolve(dirId);
    }

    private Path resolve(final String directoryId) {
        final String dirHash = cryptomator.getCryptor().fileNameCryptor().hashDirectoryId(directoryId);
        // Intermediate directory
        final Path intermediate = new Path(dataRoot, dirHash.substring(0, 2), EnumSet.of(Path.Type.directory, Path.Type.vault));
        final PathAttributes attributes = new PathAttributes();
        attributes.setDirectoryId(directoryId);
        return new Path(intermediate, dirHash.substring(2), EnumSet.of(Path.Type.directory, Path.Type.encrypted), attributes);
    }
}
