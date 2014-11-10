package ch.cyberduck.core.local;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.io.watchservice.RegisterWatchService;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @version $Id$
 */
public final class FileWatcher {
    private static Logger log = Logger.getLogger(FileWatcher.class);

    private RegisterWatchService monitor;

    private ThreadPool pool;

    public FileWatcher(final RegisterWatchService monitor) {
        this.monitor = monitor;
        this.pool = new ThreadPool(1, "watcher");
    }

    public CountDownLatch register(final Local file, final FileWatcherListener listener) throws IOException {
        // Make sure to canonicalize the watched folder
        if(!file.exists()) {
            throw new FileNotFoundException();
        }
        final Path folder = new File(file.getParent().getAbsolute()).getCanonicalFile().toPath();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Register file %s", folder));
        }
        monitor.register(folder, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        final CountDownLatch lock = new CountDownLatch(1);
        pool.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
                while(true) {
                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        lock.countDown();
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
                        if(kind == OVERFLOW) {
                            log.error(String.format("Overflow event for %s", folder));
                            break;
                        }
                        // The filename is the context of the event. May be absolute or relative path name.
                        if(matches(LocalFactory.get(event.context().toString()), LocalFactory.get(folder.toString(), file.getName()))) {
                            callback(event, listener);
                        }
                        else {
                            log.warn(String.format("Ignored file system event for unknown file %s", event.context()));
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
        return lock;
    }

    protected Local normalize(final Local file) {
        try {
            return LocalFactory.get(new File(file.getAbsolute()).getCanonicalPath());
        }
        catch(IOException e) {
            log.warn(String.format("Failure getting real path for file %s", file));
            return file;
        }
    }

    protected boolean matches(final Local context, final Local file) {
        if(!new File(context.getAbsolute()).isAbsolute()) {
            return context.getName().equals(file.getName());
        }
        return this.normalize(context).equals(this.normalize(file));
    }

    private void callback(final WatchEvent<?> event, final FileWatcherListener l) {
        final WatchEvent.Kind<?> kind = event.kind();
        if(log.isInfoEnabled()) {
            log.info(String.format("Process file system event %s for %s", kind.name(), event.context()));
        }
        if(ENTRY_MODIFY == kind) {
            l.fileWritten(LocalFactory.get(event.context().toString()));
        }
        else if(ENTRY_DELETE == kind) {
            l.fileDeleted(LocalFactory.get(event.context().toString()));
        }
        else if(ENTRY_CREATE == kind) {
            l.fileCreated(LocalFactory.get(event.context().toString()));
        }
        else {
            log.debug(String.format("Ignored file system event %s for %s", kind.name(), event.context()));
        }
    }

    public void close() {
        try {
            monitor.close();
            pool.shutdown();
        }
        catch(IOException e) {
            log.error(String.format("Failure closing file watcher monitor"), e);
        }
    }
}