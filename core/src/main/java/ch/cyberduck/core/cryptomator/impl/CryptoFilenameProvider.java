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

import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoFilenameProvider {

    private static final BaseEncoding BASE32 = BaseEncoding.base32();
    private static final int MAX_CACHE_SIZE = 5000;
    private static final String LONG_NAME_FILE_EXT = ".lng";
    private static final String METADATA_DIR_NAME = "m";

    private static final int NAME_SHORTENING_THRESHOLD = 129;

    private final Path metadataRoot;

    public CryptoFilenameProvider(final Path vault) {
        this.metadataRoot = new Path(vault, METADATA_DIR_NAME, EnumSet.of(Path.Type.directory));
    }

    public boolean isDeflated(final String filename) {
        return filename.endsWith(LONG_NAME_FILE_EXT);
    }

    public String inflate(final Session<?> session, final String shortName) throws BackgroundException {
        return new ContentReader(session).readToString(resolve(shortName));
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
        final Path metadataRoot = firstLevel.getParent();
        final Directory feature = session._getFeature(Directory.class);
        //TODO do not fail in case the folders already exist
        feature.mkdir(metadataRoot);
        feature.mkdir(firstLevel);
        feature.mkdir(secondLevel);
        final ContentWriter writer = new ContentWriter(session);
        writer.write(metadataFile, longFileNameBytes);
        return shortName;
    }

    public Path resolve(final String filename) {
        return new Path(new Path(new Path(metadataRoot, filename.substring(0, 2), EnumSet.of(Path.Type.directory)),
                filename.substring(2, 4), EnumSet.of(Path.Type.directory)), filename, EnumSet.of(Path.Type.file));
    }

    public Path getMetadataRoot() {
        return metadataRoot;
    }

    public void close() {
    }
}
