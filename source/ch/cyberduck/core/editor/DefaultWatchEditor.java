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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
public class DefaultWatchEditor extends BrowserBackgroundEditor {
    private static final Logger log = Logger.getLogger(DefaultWatchEditor.class);

    private WatchService monitor;

    private ThreadPool pool
            = new ThreadPool(1, "watcher");

    public DefaultWatchEditor(final Controller controller,
                              final Session session,
                              final Application application,
                              final Path path) {
        super(controller, session, application, path);
    }

    public DefaultWatchEditor(final Controller controller,
                              final Session session,
                              final ApplicationLauncher launcher,
                              final Application application,
                              final Path path) {
        super(controller, session, launcher, application, path);
    }

    @Override
    protected void watch(final Local local) throws IOException {
        final FileSystem fs = FileSystems.getDefault();
        monitor = fs.newWatchService();
        final java.nio.file.Path watchable = fs.getPath(local.getParent().getAbsolute());
        final WatchKey key = watchable.register(monitor,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        pool.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
                while(true) {
                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        key = monitor.take();
                    }
                    catch(ClosedWatchServiceException e) {
                        // If this watch service is closed
                        return true;
                    }
                    catch(InterruptedException e) {
                        return false;
                    }
                    for(WatchEvent<?> event : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = event.kind();
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Detected file system event %s", kind.name()));
                        }
                        if(kind == StandardWatchEventKinds.OVERFLOW) {
                            log.error(String.format("Overflow event for %s", watchable));
                            break;
                        }
                        // The filename is the context of the event.
                        if(event.context().equals((watchable.relativize(Paths.get(local.getAbsolute()))))) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Process file system event %s for %s", kind.name(), event.context()));
                            }
                            if(StandardWatchEventKinds.ENTRY_MODIFY == kind) {
                                save();
                            }
                            else if(StandardWatchEventKinds.ENTRY_DELETE == kind) {
                                delete();
                            }
                            else if(StandardWatchEventKinds.ENTRY_CREATE == kind) {
                                save();
                            }
                            else {
                                log.debug(String.format("Ignored file system event %s for %s", kind.name(), event.context()));
                            }
                        }
                        else {
                            log.debug(String.format("Ignored file system event for unknown file %s", event.context()));
                        }
                    }
                    // Reset the key -- this step is critical to receive further watch events.
                    boolean valid = key.reset();
                    if(!valid) {
                        // The key is no longer valid and the loop can exit.
                        return true;
                    }
                }
            }
        });
    }

    @Override
    public void delete() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Close monitor %s", monitor));
        }
        try {
            monitor.close();
        }
        catch(IOException e) {
            log.warn(String.format("Failure closing monitor %s", e.getMessage()));
        }
        super.delete();
    }
}

