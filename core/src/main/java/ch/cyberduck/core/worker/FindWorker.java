package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingListProgressListener;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FindWorker extends Worker<Boolean> {
    private static final Logger log = LogManager.getLogger(FindWorker.class.getName());

    private final Cache<Path> cache;
    private final Path file;

    public FindWorker(final Cache<Path> cache, final Path file) {
        this.file = file;
        this.cache = cache;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        log.debug("Find file {}", file);
        return session.getFeature(Find.class).find(file, new WorkerCanceledListProgressListener(this,
                new CachingListProgressListener(new DisabledListProgressListener(), cache)));
    }

    @Override
    public void cleanup(final Boolean result) {
        log.debug("Return {} for file {}", result, file);
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public final boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final FindWorker that = (FindWorker) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }
}
