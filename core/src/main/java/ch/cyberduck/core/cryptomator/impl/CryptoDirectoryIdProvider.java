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
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.util.UUID;
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
        public String load(final Path directoryMetafile) throws BackgroundException {
            if(!session._getFeature(Find.class).find(directoryMetafile)) {
                return UUID.randomUUID().toString();
            }
            return new ContentReader(session).readToString(directoryMetafile);
        }
    }

    public String load(final Path directoryMetafile) throws BackgroundException {
        try {
            return cache.get(directoryMetafile);
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

    /**
     * Removes the id currently associated with <code>dirFilePath</code> from cache. Useful during folder delete operations.
     * This method has no effect if the content of the given dirFile is not currently cached.
     *
     * @param directoryMetafile The directoryMetafile for which the cache should be deleted.
     */
    public void delete(final Path directoryMetafile) {
        cache.invalidate(directoryMetafile);
    }

    /**
     * Transfers ownership from the id currently associated with <code>srcDirFilePath</code> to <code>dstDirFilePath</code>.
     * Useful during folder move operations.
     * This method has no effect if the content of the source dirFile is not currently cached.
     *
     * @param sourceDirectoryMetafile The directory that contained the cached id until now.
     * @param targetDirectoryMetafile The directory that will contain the id from now on.
     */
    public void move(final Path sourceDirectoryMetafile, final Path targetDirectoryMetafile) {
        String id = cache.getIfPresent(sourceDirectoryMetafile);
        if(id != null) {
            cache.put(targetDirectoryMetafile, id);
            cache.invalidate(sourceDirectoryMetafile);
        }
    }

    public void close() {
        cache.invalidateAll();
    }
}
