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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class CryptoDirectoryProvider {

    private static final String DATA_DIR_NAME = "d";

    private static final String ROOT_DIR_ID = StringUtils.EMPTY;

    private final LoadingCache<String, Path> cache
            = CacheBuilder.newBuilder().maximumSize(
            PreferencesFactory.get().getInteger("browser.cache.size")
    ).build(CacheLoader.from(this::resolve));

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
        try {
            if(dataRoot.getParent().equals(directory)) {
                return cache.get(ROOT_DIR_ID);
            }
            final Path parent = this.toEncrypted(session, directory.getParent());
            final String cleartextName = directory.getName();
            final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
            final String dirId = cryptomator.getDirectoryIdProvider().load(session, new Path(parent, ciphertextName, EnumSet.of(Path.Type.file, Path.Type.encrypted)));
            return cache.get(dirId);
        }
        catch(ExecutionException | UncheckedExecutionException e) {
            if(e.getCause() instanceof IOException) {
                throw new DefaultIOExceptionMappingService().map((IOException) e.getCause());
            }
            if(e.getCause() instanceof BackgroundException) {
                throw (BackgroundException) e.getCause();
            }
            throw new BackgroundException(e.getCause());
        }
    }

    private Path resolve(final String directoryId) {
        final String dirHash = cryptomator.getCryptor().fileNameCryptor().hashDirectoryId(directoryId);
        // Intermediate directory
        final Path intermediate = new Path(dataRoot, dirHash.substring(0, 2), EnumSet.of(Path.Type.directory, Path.Type.encrypted, Path.Type.vault));
        final PathAttributes attributes = new PathAttributes();
        attributes.setDirectoryId(directoryId);
        return new Path(intermediate, dirHash.substring(2), EnumSet.of(Path.Type.directory, Path.Type.encrypted), attributes);
    }

    public void close() {
        cache.invalidateAll();
    }
}
