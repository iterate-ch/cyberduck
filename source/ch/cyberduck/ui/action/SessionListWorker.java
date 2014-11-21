package ch.cyberduck.ui.action;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class SessionListWorker extends Worker<AttributedList<Path>> implements ListProgressListener {
    private static final Logger log = Logger.getLogger(SessionListWorker.class);

    private Session<?> session;

    private Cache<Path> cache;

    private Path directory;

    private ListProgressListener listener;

    public SessionListWorker(final Session<?> session, final Cache<Path> cache, final Path directory,
                                final ListProgressListener listener) {
        this.session = session;
        this.cache = cache;
        this.directory = directory;
        this.listener = listener;
    }

    @Override
    public AttributedList<Path> run() throws BackgroundException {
        try {
            if(cache.isCached(directory.getReference())) {
                return cache.get(directory.getReference());
            }
            final AttributedList<Path> children = session.list(directory, this);
            cache.put(directory.getReference(), children);
            return children;
        }
        catch(ListCanceledException e) {
            final AttributedList<Path> chunk = e.getChunk();
            cache.put(directory.getReference(), chunk);
            return chunk;
        }
        catch(BackgroundException e) {
            // Cache empty listing
            cache.put(directory.getReference(), AttributedList.<Path>emptyList());
            throw e;
        }
    }

    @Override
    public void chunk(final Path parent, final AttributedList<Path> list) throws ConnectionCanceledException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Retrieved chunk of %d items in %s", list.size(), directory));
        }
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        listener.chunk(directory, list);
    }

    @Override
    public void message(final String message) {
        listener.message(message);
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
        final SessionListWorker that = (SessionListWorker) o;
        if(directory != null ? !directory.equals(that.directory) : that.directory != null) {
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
        final StringBuilder sb = new StringBuilder("SessionListWorker{");
        sb.append("directory=").append(directory);
        sb.append('}');
        return sb.toString();
    }
}