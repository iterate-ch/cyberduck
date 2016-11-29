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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class CryptoDirectoryIdProvider {

    private final LoadingCache<Path, String> cache;
    private final Session<?> session;

    public CryptoDirectoryIdProvider(final Session<?> session) {
        this.session = session;
        this.cache = CacheBuilder.newBuilder().maximumSize(
                PreferencesFactory.get().getInteger("browser.cache.size")
        ).build(new Loader());
    }

    private class Loader extends CacheLoader<Path, String> {
        @Override
        public String load(final Path directory) throws BackgroundException {
            return new ContentReader(session).readToString(directory);
        }
    }

    public String load(final Path directory) throws IOException {
        try {
            return cache.get(directory);
        }
        catch(ExecutionException | UncheckedExecutionException e) {
            if(e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e.getCause());
        }
    }

    /**
     * Removes the id currently associated with <code>dirFilePath</code> from cache. Useful during folder delete operations.
     * This method has no effect if the content of the given dirFile is not currently cached.
     *
     * @param directory The directory for which the cache should be deleted.
     */
    public void delete(Path directory) {
        cache.invalidate(directory);
    }

    /**
     * Transfers ownership from the id currently associated with <code>srcDirFilePath</code> to <code>dstDirFilePath</code>.
     * Useful during folder move operations.
     * This method has no effect if the content of the source dirFile is not currently cached.
     *
     * @param sourceDirectory The directory that contained the cached id until now.
     * @param targetDirectory The directory that will contain the id from now on.
     */
    public void move(Path sourceDirectory, Path targetDirectory) {
        String id = cache.getIfPresent(sourceDirectory);
        if(id != null) {
            cache.put(targetDirectory, id);
            cache.invalidate(sourceDirectory);
        }
    }

    public void close() {
        cache.invalidateAll();
    }
}
