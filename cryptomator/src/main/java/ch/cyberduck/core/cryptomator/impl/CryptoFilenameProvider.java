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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoFilenameProvider {
    private static final Logger log = Logger.getLogger(CryptoFilenameProvider.class);

    private static final BaseEncoding BASE32 = BaseEncoding.base32();
    private static final String LONG_NAME_FILE_EXT = ".lng";
    private static final String METADATA_DIR_NAME = "m";

    private static final int NAME_SHORTENING_THRESHOLD = 129;

    private final Path metadataRoot;

    public CryptoFilenameProvider(final Path vault) {
        this.metadataRoot = new Path(vault, METADATA_DIR_NAME, vault.getType());
    }

    public boolean isDeflated(final String filename) {
        return filename.endsWith(LONG_NAME_FILE_EXT);
    }

    public String inflate(final Session<?> session, final String shortName) throws BackgroundException {
        return new ContentReader(session).read(resolve(shortName));
    }

    public String deflate(final Session<?> session, final String filename) throws BackgroundException {
        if(filename.length() < NAME_SHORTENING_THRESHOLD) {
            return filename;
        }
        final byte[] longFileNameBytes = filename.getBytes(UTF_8);
        final byte[] hash = MessageDigestSupplier.SHA1.get().digest(longFileNameBytes);
        final String shortName = BASE32.encode(hash) + LONG_NAME_FILE_EXT;
        final Path metadataFile = this.resolve(shortName);
        final Path secondLevel = metadataFile.getParent();
        final Path firstLevel = secondLevel.getParent();
        final Directory mkdir = session._getFeature(Directory.class);
        final Find find = session._getFeature(Find.class);
        if(!find.find(metadataRoot)) {
            mkdir.mkdir(metadataRoot);
        }
        if(!find.find(firstLevel)) {
            mkdir.mkdir(firstLevel);
        }
        if(!find.find(secondLevel)) {
            mkdir.mkdir(secondLevel);
        }
        new ContentWriter(session).write(metadataFile, longFileNameBytes);
        if(log.isInfoEnabled()) {
            log.info(String.format("Deflated %s to %s", filename, shortName));
        }
        return shortName;
    }

    public Path resolve(final String filename) {
        // Intermediate directory
        final Path first = new Path(metadataRoot, filename.substring(0, 2), metadataRoot.getType());
        // Intermediate directory
        final Path second = new Path(first, filename.substring(2, 4), metadataRoot.getType());
        return new Path(second, filename, EnumSet.of(Path.Type.file, Path.Type.encrypted, Path.Type.vault));
    }
}
