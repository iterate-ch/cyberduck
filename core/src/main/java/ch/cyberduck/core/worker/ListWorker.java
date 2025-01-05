package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

public class ListWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = LogManager.getLogger(ListWorker.class);

    private final Cache<Path> cache;
    private final Path directory;
    private final ListProgressListener listener;

    public ListWorker(final Cache<Path> cache, final Path directory, final ListProgressListener listener) {
        this.cache = cache;
        this.directory = directory;
        this.listener = new WorkerCanceledListProgressListener(this, new CachingListProgressListener(cache, listener));
    }

    @Override
    public AttributedList<Path> run(final Session<?> session) throws BackgroundException {
        try {
            final AttributedList<Path> list;
            listener.reset();
            if(this.isCached()) {
                list = cache.get(directory);
                listener.chunk(directory, list);
            }
            else {
                final ListService service = session.getFeature(ListService.class);
                log.debug("Run with feature {}", service);
                list = service.list(directory, listener);
                if(list.isEmpty()) {
                    log.debug("Notify listener {} with chunk for {}", listener, directory);
                    listener.chunk(directory, list);
                }
            }
            return list;
        }
        catch(ListCanceledException e) {
            log.warn("Return partial directory listing for {}", directory);
            return e.getChunk();
        }
    }

    protected boolean isCached() {
        return cache.isValid(directory);
    }

    @Override
    public void cleanup(final AttributedList<Path> list, final BackgroundException failure) {
        listener.cleanup(directory, list, Optional.ofNullable(failure));
        super.cleanup(list, failure);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                directory.getName());
    }

    @Override
    public AttributedList<Path> initialize() {
        return AttributedList.emptyList();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final ListWorker that = (ListWorker) o;
        if(!Objects.equals(directory, that.directory)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return directory != null ? directory.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ListWorker{");
        sb.append("directory=").append(directory);
        sb.append('}');
        return sb.toString();
    }
}
