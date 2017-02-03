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
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class CryptoDirectoryProvider {
    private static final Logger log = Logger.getLogger(CryptoDirectoryProvider.class);

    private static final String DATA_DIR_NAME = "d";
    private static final String ROOT_DIR_ID = StringUtils.EMPTY;

    private final Path dataRoot;
    private final CryptoVault cryptomator;

    private final RandomStringService random
            = new UUIDRandomStringService();

    public CryptoDirectoryProvider(final Path vault, final CryptoVault cryptomator) {
        this.dataRoot = new Path(vault, DATA_DIR_NAME, vault.getType());
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Encrypted filename %s to %s", filename, ciphertextName));
        }
        return cryptomator.getFilenameProvider().deflate(session, ciphertextName);
    }

    /**
     * @param directory Clear text
     */
    public Path toEncrypted(final Session<?> session, final Path directory) throws BackgroundException {
        if(directory.getType().contains(Path.Type.directory)) {
            String directoryId;
            if(dataRoot.getParent().equals(directory)) {
                directoryId = ROOT_DIR_ID;
            }
            else {
                if(StringUtils.isBlank(directory.attributes().getDirectoryId())) {
                    final Path parent = this.toEncrypted(session, directory.getParent());
                    final String cleartextName = directory.getName();
                    final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
                    // Read directory id from file
                    try {
                        directoryId = new ContentReader(session).read(new Path(parent, ciphertextName, EnumSet.of(Path.Type.file, Path.Type.encrypted)));
                    }
                    catch(NotfoundException e) {
                        log.warn(String.format("Missing directory ID for folder %s", directory));
                        directoryId = random.random();
                    }
                }
                else {
                    directoryId = directory.attributes().getDirectoryId();
                }
            }
            final String dirHash = cryptomator.getCryptor().fileNameCryptor().hashDirectoryId(directoryId);
            // Intermediate directory
            final Path intermediate = new Path(dataRoot, dirHash.substring(0, 2), dataRoot.getType());
            final PathAttributes attributes = new PathAttributes();
            // Save directory id for use in vault
            attributes.setDirectoryId(directoryId);
            // Add encrypted type
            final EnumSet<AbstractPath.Type> type = EnumSet.copyOf(directory.getType());
            type.add(Path.Type.encrypted);
            return new Path(intermediate, dirHash.substring(2), type, attributes);
        }
        throw new NotfoundException(directory.getAbsolute());
    }
}
