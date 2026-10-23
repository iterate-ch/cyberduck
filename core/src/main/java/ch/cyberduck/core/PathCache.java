package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PathCache extends AbstractCache<Path> {
    private static final Logger log = LogManager.getLogger(PathCache.class);

    private static final CacheReference<Path> NULL_KEY = new DefaultPathPredicate(Home.root());

    private static final PathCache EMPTY = new PathCache(0) {
        @Override
        public AttributedList<Path> put(final Path key, final AttributedList<Path> children) {
            return AttributedList.emptyList();
        }

        @Override
        public void lock(final Path parent) {
            // Nothing cached to wait for
        }

        @Override
        public void unlock(final Path parent) {
            //
        }
    };

    public static PathCache empty() {
        return EMPTY;
    }

    public PathCache(final int size) {
        super(size);
    }

    public PathCache(final int size, final Long expireDuration) {
        super(size, expireDuration);
    }

    @Override
    public CacheReference<Path> reference(final Path file) {
        if(null == file) {
            return NULL_KEY;
        }
        return new DefaultPathPredicate(file);
    }

    /**
     * Locks for directories currently looked up. Removed when no longer held or awaited.
     */
    private final Map<CacheReference<Path>, DirectoryLock> locks = new ConcurrentHashMap<>();

    private static final class DirectoryLock {
        private final ReentrantLock lock = new ReentrantLock();
        /**
         * Number of acquisitions held or awaited
         */
        private int references;
    }

    @Override
    public void lock(final Path directory) throws ConnectionCanceledException {
        final CacheReference<Path> reference = null == directory ? NULL_KEY : new SimplePathPredicate(directory);
        final DirectoryLock entry = locks.compute(reference, (key, value) -> {
            final DirectoryLock l = null == value ? new DirectoryLock() : value;
            l.references++;
            return l;
        });
        try {
            entry.lock.lockInterruptibly();
        }
        catch(InterruptedException e) {
            this.release(reference);
            Thread.currentThread().interrupt();
            throw new ConnectionCanceledException(e);
        }
    }

    @Override
    public void unlock(final Path directory) {
        final CacheReference<Path> reference = null == directory ? NULL_KEY : new SimplePathPredicate(directory);
        final DirectoryLock entry = locks.get(reference);
        if(null == entry || !entry.lock.isHeldByCurrentThread()) {
            log.error("Directory {} not locked by current thread", directory);
            return;
        }
        entry.lock.unlock();
        this.release(reference);
    }

    private void release(final CacheReference<Path> reference) {
        locks.compute(reference, (key, value) -> null == value || --value.references <= 0 ? null : value);
    }
}
