package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2016 Sebastian Stenzel and others.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.ContentReader;

import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.UncheckedExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LongFileNameProvider {

    private static final BaseEncoding BASE32 = BaseEncoding.base32();
    private static final int MAX_CACHE_SIZE = 5000;
    private static final String LONG_NAME_FILE_EXT = ".lng";

    private static final int NAME_SHORTENING_THRESHOLD = 129;

    private final Path metadataRoot;
    private final Session<?> session;
    private final LoadingCache<String, String> ids;

    public LongFileNameProvider(final Path pathToVault, final Session<?> session) {
        this.metadataRoot = new Path(pathToVault, Constants.METADATA_DIR_NAME, EnumSet.of(Path.Type.directory));
        this.session = session;
        this.ids = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build(new Loader());
    }

    private class Loader extends CacheLoader<String, String> {
        @Override
        public String load(final String shortName) throws BackgroundException {
            return new ContentReader(session).readToString(resolveMetadataFile(shortName));
        }
    }

    public static boolean isDeflated(final String possiblyDeflatedFileName) {
        return possiblyDeflatedFileName.endsWith(LONG_NAME_FILE_EXT);
    }

    public String inflate(final String shortFileName) throws IOException {
        try {
            return ids.get(shortFileName);
        }
        catch(ExecutionException e) {
            if(e.getCause() instanceof IOException || e.getCause() instanceof UncheckedIOException) {
                throw new IOException(e);
            }
            else {
                throw new UncheckedExecutionException("Unexpected exception", e);
            }
        }
    }

    public String deflate(final String longFileName) throws IOException {
        if(longFileName.length() < NAME_SHORTENING_THRESHOLD) {
            return longFileName;
        }
        byte[] longFileNameBytes = longFileName.getBytes(UTF_8);
        byte[] hash = MessageDigestSupplier.SHA1.get().digest(longFileNameBytes);
        String shortName = BASE32.encode(hash) + LONG_NAME_FILE_EXT;
        if(ids.getIfPresent(shortName) == null) {
            ids.put(shortName, longFileName);
            // TODO markuskreusch, overheadhunter: do we really want to persist this at this point?...
            // ...maybe the caller only wanted to know if a file exists without creating anything.
            Path file = resolveMetadataFile(shortName);
            Path fileDir = file.getParent();
            assert fileDir != null : "resolveMetadataFile returned path to a file";

            //TODO yla: NOCH ZU IMPLEMENTIEREN
            throw new UnsupportedOperationException();
//            Files.createDirectories(fileDir);
//            Files.write(file, longFileNameBytes);
        }
        return shortName;
    }

    private Path resolveMetadataFile(final String shortName) {
        return new Path(new Path(new Path(metadataRoot, shortName.substring(0, 2), EnumSet.of(Path.Type.directory)),
                shortName.substring(2, 4), EnumSet.of(Path.Type.directory)), shortName, EnumSet.of(Path.Type.directory));
    }
}
