package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Worker<T> {

    private final AtomicBoolean canceled
            = new AtomicBoolean();

    protected final PathContainerService containerService = new PathContainerService();

    protected String toString(final List<Path> files) {
        if(files.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final String name = files.get(0).getName();
        if(files.size() > 1) {
            return String.format("%s… (%s) (%d)", name, LocaleFactory.localizedString("Multiple files"), files.size());
        }
        return String.format("%s…", name);
    }

    protected Set<Path> getContainers(final List<Path> files) {
        final Set<Path> containers = new HashSet<>();
        for(Path file : files) {
            containers.add(containerService.getContainer(file));
        }
        return containers;
    }

    public T run(final Session<?> session) throws BackgroundException {
        throw new ConnectionCanceledException();
    }

    public void cleanup(T result) {
        //
    }

    public String getActivity() {
        return LocaleFactory.localizedString("Unknown");
    }

    public void cancel() {
        canceled.set(true);
    }

    public boolean isCanceled() {
        return canceled.get();
    }

    /**
     * Default result when execute fails with exception
     */
    public T initialize() {
        return null;
    }

    public static <T> Worker<T> empty() {
        return new Worker<T>() {
            @Override
            public T run(final Session<?> session) throws BackgroundException {
                return null;
            }
        };
    }

    public void reset() throws BackgroundException {
        //
    }

    public interface RecursiveCallback<T> {
        /**
         * @return True to descend into directories
         */
        boolean recurse(Path directory, T value);
    }
}
