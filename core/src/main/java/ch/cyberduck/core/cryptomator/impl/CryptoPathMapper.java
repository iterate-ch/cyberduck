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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CryptoPathMapper {

    private static final String DATA_DIR_NAME = "d";

    private static final String ROOT_DIR_ID = StringUtils.EMPTY;
    private static final int MAX_CACHED_DIR_PATHS = 1000;

    private final LoadingCache<String, Path> directoryPathCache;
    private final Path vault;
    private final CryptoVault cryptomator;

    public CryptoPathMapper(final Path vault, final CryptoVault cryptomator) {
        this.vault = new Path(vault, DATA_DIR_NAME, EnumSet.of(Path.Type.directory));
        this.cryptomator = cryptomator;
        this.directoryPathCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_DIR_PATHS).build(CacheLoader.from(this::resolve));
    }

    /**
     * @param directoryId Directory id
     * @param filename    Clear text filename
     * @param type        File type
     * @return Encrypted filename
     */
    public String getCiphertextFilename(final String directoryId, final String filename, final EnumSet<AbstractPath.Type> type) throws IOException {
        final String prefix = type.contains(Path.Type.directory) ? Constants.DIR_PREFIX : "";
        final String ciphertextName = String.format("%s%s", prefix,
                cryptomator.getCryptor().fileNameCryptor().encryptFilename(filename, directoryId.getBytes(StandardCharsets.UTF_8)));
        return cryptomator.getLongFileNameProvider().deflate(ciphertextName);
    }

    /**
     * @param directory Clear text
     */
    public Directory getCiphertextDirectory(final Path directory) throws IOException {
        if(vault.getParent().getAbsolute().equals(directory.getAbsolute())) {
            return new Directory(ROOT_DIR_ID, directoryPathCache.getUnchecked(ROOT_DIR_ID));
        }
        else {
            final Directory parent = this.getCiphertextDirectory(directory.getParent());
            final String cleartextName = directory.getName();
            final String ciphertextName = this.getCiphertextFilename(parent.id, cleartextName, EnumSet.of(Path.Type.directory));
            final String dirId = cryptomator.getDirectoryIdProvider().load(new Path(parent.path, ciphertextName, EnumSet.of(Path.Type.file)));
            return new Directory(dirId, directoryPathCache.getUnchecked(dirId));
        }
    }

    private Path resolve(final String directoryId) {
        final String dirHash = cryptomator.getCryptor().fileNameCryptor().hashDirectoryId(directoryId);
        return new Path(new Path(vault, dirHash.substring(0, 2), EnumSet.of(Path.Type.directory)), dirHash.substring(2), EnumSet.of(Path.Type.directory));
    }

    public static final class Directory {
        public final String id;
        public final Path path;

        public Directory(final String id, final Path path) {
            this.id = id;
            this.path = path;
        }
    }
}
