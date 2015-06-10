package ch.cyberduck.fs;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.threading.WorkerBackgroundAction;

/**
 * @version $Id$
 */
public class FilesystemBackgroundAction extends WorkerBackgroundAction<Void> {

    public FilesystemBackgroundAction(final Controller controller,
                                      final ConnectionService connection,
                                      final Session<?> session,
                                      final PathCache cache) {
        this(controller, FilesystemFactory.get(connection, session, cache), connection, session, cache);
    }

    public FilesystemBackgroundAction(final Controller controller,
                                      final Filesystem fs,
                                      final ConnectionService connection,
                                      final Session<?> session,
                                      final PathCache cache) {
        this(controller, connection, session, cache, new FilesystemWorker(session, fs));
    }

    public FilesystemBackgroundAction(final Controller controller,
                                      final ConnectionService connection,
                                      final Session<?> session,
                                      final PathCache cache,
                                      final FilesystemWorker worker) {
        this(controller, connection, session, cache, worker, controller, controller);
    }

    public FilesystemBackgroundAction(final Controller controller,
                                      final ConnectionService connection,
                                      final Session<?> session,
                                      final Cache<Path> cache,
                                      final FilesystemWorker worker,
                                      final ProgressListener progress,
                                      final TranscriptListener transcript) {
        super(connection, controller, session, cache, worker, progress, transcript);
    }

    @Override
    public Object lock() {
        // Allow to run concurrently
        return null;
    }
}
