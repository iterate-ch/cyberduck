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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.exception.BackgroundException;

import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.UncheckedExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoFilenameProvider {

    private static final BaseEncoding BASE32 = BaseEncoding.base32();
    private static final int MAX_CACHE_SIZE = 5000;
    private static final String LONG_NAME_FILE_EXT = ".lng";
    private static final String METADATA_DIR_NAME = "m";

    private static final int NAME_SHORTENING_THRESHOLD = 129;

    private final LoadingCache<String, String> cache
            = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build(new Loader());

    private final Session<?> session;
    private final Path metadataRoot;

    public CryptoFilenameProvider(final Session<?> session, final Path vault) {
        this.metadataRoot = new Path(vault, METADATA_DIR_NAME, EnumSet.of(Path.Type.directory));
        this.session = session;
    }

    private class Loader extends CacheLoader<String, String> {
        @Override
        public String load(final String shortName) throws BackgroundException {
            return new ContentReader(session).readToString(resolve(shortName));
        }
    }

    public boolean isDeflated(final String filename) {
        return filename.endsWith(LONG_NAME_FILE_EXT);
    }

    public String inflate(final String filename) throws BackgroundException {
        try {
            return cache.get(filename);
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

    public String deflate(final String filename) {
        if(filename.length() < NAME_SHORTENING_THRESHOLD) {
            return filename;
        }
        final byte[] longFileNameBytes = filename.getBytes(UTF_8);
        final byte[] hash = MessageDigestSupplier.SHA1.get().digest(longFileNameBytes);
        final String shortName = BASE32.encode(hash) + LONG_NAME_FILE_EXT;
        if(cache.getIfPresent(shortName) == null) {
            cache.put(shortName, filename);
            Path file = this.resolve(shortName);
            Path directory = file.getParent();

            //TODO yla: NOCH ZU IMPLEMENTIEREN
            throw new UnsupportedOperationException();
//            Files.createDirectories(fileDir);
//            Files.write(file, longFileNameBytes);
        }
        return shortName;
    }

    private Path resolve(final String filename) {
        return new Path(new Path(new Path(metadataRoot, filename.substring(0, 2), EnumSet.of(Path.Type.directory)),
                filename.substring(2, 4), EnumSet.of(Path.Type.directory)), filename, EnumSet.of(Path.Type.directory));
    }

    public void close() {
        cache.invalidateAll();
    }
}
