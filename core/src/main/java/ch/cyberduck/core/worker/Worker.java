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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class Worker<T> {
    private static final Logger log = LogManager.getLogger(Worker.class);

    /**
     * Maximum number of elements printed in log output
     */
    private static final int ABBREVIATE_LIMIT = 10;

    private final AtomicBoolean canceled
            = new AtomicBoolean();

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

    /**
     * @param files Files
     * @return String representation for logging limited to first elements with total count
     */
    protected static String abbreviate(final Collection<?> files) {
        if(files.size() <= ABBREVIATE_LIMIT) {
            return files.toString();
        }
        return files.stream().limit(ABBREVIATE_LIMIT).map(String::valueOf)
                .collect(Collectors.joining(", ", "[", String.format(", … (%d total)]", files.size())));
    }

    protected Set<Path> getContainers(final PathContainerService containerService, final List<Path> files) {
        final Set<Path> containers = new HashSet<>();
        for(Path file : files) {
            containers.add(containerService.getContainer(file));
        }
        return containers;
    }

    public T run(final Session<?> session) throws BackgroundException {
        throw new ConnectionCanceledException();
    }

    /**
     * Override to handle result regardless of failure
     *
     * @param result Return value from worker
     */
    public void cleanup(final T result) {
        log.debug("Cleanup with result {}", result);
    }

    /**
     * Override to handle result with optional failure
     *
     * @param result  Return value from worker
     * @param failure Null on success
     */
    public void cleanup(final T result, final BackgroundException failure) {
        log.debug("Cleanup with result {} and failure {}", result, failure);
        this.cleanup(result);
    }

    public String getActivity() {
        return LocaleFactory.localizedString("Unknown");
    }

    public void cancel() {
        log.warn("Cancel worker {}", this);
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
            public T run(final Session<?> session) {
                return null;
            }
        };
    }

    public interface RecursiveCallback<T> {
        /**
         * @return True to descend into directories
         */
        boolean recurse(Path directory, T value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Worker{");
        sb.append("canceled=").append(canceled);
        sb.append('}');
        return sb.toString();
    }
}
