package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.io.watchservice.NIOEventWatchService;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * An editor listing for file system notifications on a particular folder
 */
public class DefaultWatchEditor extends AbstractEditor {
    private static final Logger log = LogManager.getLogger(DefaultWatchEditor.class);

    private final FileWatcher watcher;

    public DefaultWatchEditor(final Host host, final Path file, final ProgressListener listener) {
        this(host, file, listener, new FileWatcher(new NIOEventWatchService()));
    }

    public DefaultWatchEditor(final Host host, final Path file, final ProgressListener listener, final FileWatcher watcher) {
        super(host, file, listener);
        this.watcher = watcher;
    }

    @Override
    protected void watch(final Application application, final Local temporary, final FileWatcherListener listener, final ApplicationQuitCallback quit) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("Register {} in file watcher {}", temporary, watcher);
        }
        Uninterruptibles.awaitUninterruptibly(watcher.register(temporary, listener));
        if(log.isDebugEnabled()) {
            log.debug("Successfully registered {} in file watcher {}", temporary, watcher);
        }
    }

    @Override
    public void close() {
        if(log.isDebugEnabled()) {
            log.debug("Close watcher {}", watcher);
        }
        watcher.close();
    }
}

