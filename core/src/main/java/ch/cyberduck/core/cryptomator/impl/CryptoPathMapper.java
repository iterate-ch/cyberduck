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
    private final Path dataRoot;
    private final CryptoVault cryptomator;

    public CryptoPathMapper(final Path vault, final CryptoVault cryptomator) {
        this.dataRoot = new Path(vault, DATA_DIR_NAME, EnumSet.of(Path.Type.directory));
        this.cryptomator = cryptomator;
        this.directoryPathCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_DIR_PATHS).build(CacheLoader.from(this::resolveDirectory));
    }

    public String getCiphertextFileName(final String dirId, final String cleartextName, final EnumSet<AbstractPath.Type> type) throws IOException {
        final String prefix = type.contains(Path.Type.directory) ? Constants.DIR_PREFIX : "";
        final String ciphertextName = prefix + cryptomator.getCryptor().fileNameCryptor().encryptFilename(cleartextName, dirId.getBytes(StandardCharsets.UTF_8));
        return cryptomator.getLongFileNameProvider().deflate(ciphertextName);
    }

    public Directory getCiphertextDir(final Path cleartextPath) throws IOException {
        if(dataRoot.getParent().getAbsolute().equals(cleartextPath.getAbsolute())) {
            return new Directory(ROOT_DIR_ID, directoryPathCache.getUnchecked(ROOT_DIR_ID));
        }
        else {
            final Directory parent = getCiphertextDir(cleartextPath.getParent());
            final String cleartextName = cleartextPath.getName();
            final String ciphertextName = getCiphertextFileName(parent.dirId, cleartextName, EnumSet.of(Path.Type.directory));
            final String dirId = cryptomator.getDirectoryIdProvider().load(new Path(parent.path, ciphertextName, EnumSet.of(Path.Type.file)));
            return new Directory(dirId, directoryPathCache.getUnchecked(dirId));
        }
    }

    private Path resolveDirectory(final String dirId) {
        final String dirHash = cryptomator.getCryptor().fileNameCryptor().hashDirectoryId(dirId);
        return new Path(new Path(dataRoot, dirHash.substring(0, 2), EnumSet.of(Path.Type.directory)), dirHash.substring(2), EnumSet.of(Path.Type.directory));
    }

    public static final class Directory {
        public final String dirId;
        public final Path path;

        public Directory(final String dirId, final Path path) {
            this.dirId = dirId;
            this.path = path;
        }
    }
}
