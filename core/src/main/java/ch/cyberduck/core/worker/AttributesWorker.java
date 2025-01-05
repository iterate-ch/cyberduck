package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MemoryListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

public class AttributesWorker extends Worker<PathAttributes> {
    private static final Logger log = LogManager.getLogger(AttributesWorker.class.getName());

    private final Cache<Path> cache;
    private final Path file;
    private final MemoryListProgressListener memory;

    public AttributesWorker(final Cache<Path> cache, final Path file) {
        this.cache = cache;
        this.file = file;
        this.memory = new MemoryListProgressListener(new WorkerCanceledListProgressListener(this,
                new CachingListProgressListener(cache)));
    }

    @Override
    public PathAttributes run(final Session<?> session) throws BackgroundException {
        return new CachingAttributesFinderFeature(session, cache, session.getFeature(AttributesFinder.class)).find(file, memory);
    }

    protected boolean isCached(final Path directory) {
        return cache.isValid(directory);
    }

    @Override
    public void cleanup(final PathAttributes result, final BackgroundException e) {
        final Path directory = file.getParent();
        final AttributedList<Path> list = memory.getContents();
        log.debug("Cache directory listing {} for {}", list, directory);
        memory.cleanup(directory, list, Optional.ofNullable(e));
    }

    @Override
    public PathAttributes initialize() {
        return PathAttributes.EMPTY;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final AttributesWorker that = (AttributesWorker) o;
        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"), file.getName());
    }
}
