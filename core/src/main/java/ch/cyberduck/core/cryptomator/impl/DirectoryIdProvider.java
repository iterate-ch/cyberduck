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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.ContentReader;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DirectoryIdProvider {

    private static final int MAX_CACHE_SIZE = 5000;

    private final LoadingCache<Path, String> ids;
    private final Session<?> session;

    public DirectoryIdProvider(final Session<?> session) {
        this.session = session;
        this.ids = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build(new Loader());
    }

    private class Loader extends CacheLoader<Path, String> {
        @Override
        public String load(final Path dirFilePath) throws BackgroundException {
            try {
                return new ContentReader(session).readToString(dirFilePath);
            }
            catch(BackgroundException e) {
                return UUID.randomUUID().toString();
            }
        }
    }

    public String load(final Path dirFilePath) throws IOException {
        try {
            return ids.get(dirFilePath);
        }
        catch(ExecutionException e) {
            throw new IOException("Failed to load contents of directory file at path " + dirFilePath, e);
        }
    }

    /**
     * Removes the id currently associated with <code>dirFilePath</code> from cache. Useful during folder delete operations.
     * This method has no effect if the content of the given dirFile is not currently cached.
     *
     * @param dirFilePath The dirFile for which the cache should be deleted.
     */
    public void delete(Path dirFilePath) {
        ids.invalidate(dirFilePath);
    }

    /**
     * Transfers ownership from the id currently associated with <code>srcDirFilePath</code> to <code>dstDirFilePath</code>.
     * Useful during folder move operations.
     * This method has no effect if the content of the source dirFile is not currently cached.
     *
     * @param srcDirFilePath The dirFile that contained the cached id until now.
     * @param dstDirFilePath The dirFile that will contain the id from now on.
     */
    public void move(Path srcDirFilePath, Path dstDirFilePath) {
        String id = ids.getIfPresent(srcDirFilePath);
        if(id != null) {
            ids.put(dstDirFilePath, id);
            ids.invalidate(srcDirFilePath);
        }
    }
}
