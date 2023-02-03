package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.util.EnumSet;

import com.google.common.io.BaseEncoding;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoFilenameV6Provider implements CryptoFilename {
    private static final Logger log = LogManager.getLogger(CryptoFilenameV6Provider.class);

    private static final BaseEncoding BASE32 = BaseEncoding.base32();
    private static final String LONG_NAME_FILE_EXT = ".lng";
    private static final String METADATA_DIR_NAME = "m";

    public static final int DEFAULT_NAME_SHORTENING_THRESHOLD = 130;

    private final int shorteningThreshold;
    private final Path metadataRoot;

    private final LRUCache<String, String> cache = LRUCache.build(
        PreferencesFactory.get().getLong("cryptomator.cache.size"));

    public CryptoFilenameV6Provider(final Path vault) {
        this(vault, DEFAULT_NAME_SHORTENING_THRESHOLD);
    }

    public CryptoFilenameV6Provider(final Path vault, final int shorteningThreshold) {
        this.metadataRoot = new Path(vault, METADATA_DIR_NAME, vault.getType());
        this.shorteningThreshold = shorteningThreshold;
    }

    @Override
    public boolean isDeflated(final String filename) {
        return filename.endsWith(LONG_NAME_FILE_EXT);
    }

    @Override
    public boolean isValid(final String filename) {
        return true;
    }

    @Override
    public String inflate(final Session<?> session, final String shortName) throws BackgroundException {
        return new ContentReader(session).read(this.resolve(shortName));
    }

    @Override
    public String deflate(final Session<?> session, final String filename) throws BackgroundException {
        if(filename.length() < shorteningThreshold) {
            return filename;
        }
        if(cache.contains(filename)) {
            return cache.get(filename);
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
            mkdir.mkdir(metadataRoot, new TransferStatus());
        }
        if(!find.find(firstLevel)) {
            mkdir.mkdir(firstLevel, new TransferStatus());
        }
        if(!find.find(secondLevel)) {
            mkdir.mkdir(secondLevel, new TransferStatus());
        }
        if(!find.find(metadataFile)) {
            new ContentWriter(session).write(metadataFile, longFileNameBytes);
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Deflated %s to %s", filename, shortName));
        }
        cache.put(filename, shortName);
        return shortName;
    }

    @Override
    public Path resolve(final String filename) {
        // Intermediate directory
        final Path first = new Path(metadataRoot, filename.substring(0, 2), metadataRoot.getType());
        // Intermediate directory
        final Path second = new Path(first, filename.substring(2, 4), metadataRoot.getType());
        return new Path(second, filename, EnumSet.of(Path.Type.file, Path.Type.encrypted, Path.Type.vault));
    }

    @Override
    public void invalidate(final String filename) {
        cache.remove(filename);
    }

    @Override
    public void destroy() {
        cache.clear();
    }
}
