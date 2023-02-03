package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.util.Iterator;

public abstract class AbstractListService<T> implements ListService {
    private static final Logger log = LogManager.getLogger(AbstractListService.class);

    private final GraphFileIdProvider fileid;

    public AbstractListService(final GraphFileIdProvider fileid) {
        this.fileid = fileid;
    }

    @Override
    public final AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final Iterator<T> iterator = getIterator(directory);
        final boolean filtering = isFiltering(directory);
        try {
            while(iterator.hasNext()) {
                final T metadata;
                try {
                    metadata = iterator.next();
                }
                catch(Exception e) {
                    log.warn(e.getMessage());
                    continue;
                }
                if(filtering && !filter(metadata)) {
                    continue;
                }
                children.add(toPath(metadata, directory));
            }
        }
        catch(OneDriveRuntimeException e) { // this catches iterator.hasNext in iterate()
            throw new GraphExceptionMappingService(fileid).map("Listing directory {0} failed", e.getCause(), directory);
        }

        postList(children);
        listener.chunk(directory, children);
        return children;
    }

    protected abstract Iterator<T> getIterator(final Path directory) throws BackgroundException;

    protected abstract Path toPath(final T metadata, final Path directory);

    protected boolean isFiltering(final Path directory) {
        return false;
    }

    protected boolean filter(final T metadata) {
        return true;
    }

    protected void postList(final AttributedList<Path> list) {
    }
}
